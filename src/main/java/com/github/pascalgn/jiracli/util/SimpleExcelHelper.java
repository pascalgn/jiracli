/*
 * Copyright 2016 Pascal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pascalgn.jiracli.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class SimpleExcelHelper implements ExcelHelper {
    private static final String SHARED_STRINGS = "xl/sharedStrings.xml";
    private static final String WORKBOOK = "xl/workbook.xml";
    private static final String SHEET_PREFIX = "xl/worksheets/sheet";
    private static final String SHEET_SUFFIX = ".xml";

    @Override
    public void parseWorkbook(InputStream inputStream, CellHandler cellHandler) {
        parse(inputStream, null, cellHandler);
    }

    @Override
    public void parseWorkbook(InputStream inputStream, List<String> sheets, CellHandler cellHandler) {
        Objects.requireNonNull(sheets);
        parse(inputStream, sheets, cellHandler);
    }

    private void parse(InputStream inputStream, List<String> sheetList, CellHandler cellHandler) {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser;
        try {
            saxParser = saxParserFactory.newSAXParser();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        } catch (SAXException e) {
            throw new IllegalStateException(e);
        }

        ZipInputStream zipInputStream = new ZipInputStream(inputStream);

        InputStream noCloseInputStream = new ForwardingInputStream(zipInputStream) {
            @Override
            public void close() throws IOException {
                // prevent SAX from closing the stream
            }
        };

        Map<Integer, String> sharedStrings = new HashMap<Integer, String>();
        Map<Integer, String> sheets = new HashMap<Integer, String>();

        SharedStringsHandler sharedStringsHandler = new SharedStringsHandler(sharedStrings);
        WorkbookHandler workbookHandler = new WorkbookHandler(sheets);
        SheetHandler sheetHandler = new SheetHandler(sharedStrings, cellHandler);

        try {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.equals(SHARED_STRINGS)) {
                    saxParser.parse(noCloseInputStream, sharedStringsHandler);
                }

                if (name.contentEquals(WORKBOOK)) {
                    saxParser.parse(noCloseInputStream, workbookHandler);
                }

                if (name.startsWith(SHEET_PREFIX) && name.endsWith(SHEET_SUFFIX)) {
                    if (sheetList != null) {
                        int sheetId = getSheetId(name);
                        String sheetName = sheets.get(sheetId);
                        if (!sheetList.contains(sheetName)) {
                            continue;
                        }
                    }
                    saxParser.parse(noCloseInputStream, sheetHandler);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (SAXException e) {
            throw new IllegalStateException(e);
        }
    }

    private static int getSheetId(String name) {
        try {
            return Integer.parseInt(name.substring(SHEET_PREFIX.length(), name.length() - SHEET_SUFFIX.length()));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static class SharedStringsHandler extends DefaultHandler {
        private final Map<Integer, String> sharedStrings;

        private int index;
        private StringBuilder str;

        public SharedStringsHandler(Map<Integer, String> sharedStrings) {
            this.sharedStrings = sharedStrings;
            this.index = -1;
            this.str = new StringBuilder();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException {
            if (qName.equals("si")) {
                ++index;
                str.setLength(0);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals("si")) {
                sharedStrings.put(index, str.toString());
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            str.append(ch, start, length);
        }
    }

    private static class WorkbookHandler extends DefaultHandler {
        private final Map<Integer, String> sheets;

        public WorkbookHandler(Map<Integer, String> sheets) {
            this.sheets = sheets;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException {
            if (qName.equals("sheet")) {
                String name = attr.getValue("name");
                int id = Integer.parseInt(attr.getValue("sheetId"));
                if (sheets.containsKey(id)) {
                    throw new IllegalStateException("Duplicate sheet id: " + id);
                }
                if (sheets.containsValue(name)) {
                    throw new IllegalStateException("Duplicate sheet name: " + name);
                }
                sheets.put(id, name);
            }
        }
    }

    private static class SheetHandler extends DefaultHandler {
        private final Map<Integer, String> sharedStrings;
        private final CellHandler cellHandler;

        private Integer row;
        private String column;

        private StringBuilder cellValue;
        private Integer cellStringIndex;

        public SheetHandler(Map<Integer, String> sharedStrings, CellHandler cellHandler) {
            this.sharedStrings = sharedStrings;
            this.cellHandler = cellHandler;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException {
            if (qName.equals("row")) {
                row = Integer.valueOf(attr.getValue("r"));
            } else if (qName.equals("c")) {
                column = getColumn(attr.getValue("r"));
                String type = attr.getValue("t");
                if (type != null && type.equals("s")) {
                    cellValue = null;
                } else {
                    cellValue = new StringBuilder();
                }
            }
        }

        private static String getColumn(String str) {
            for (int i = 0; i < str.length(); i++) {
                if (Character.isDigit(str.charAt(i))) {
                    return str.substring(0, i);
                }
            }
            return str;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals("row")) {
                row = null;
            } else if (qName.equals("c")) {
                if (cellValue == null) {
                    String str = sharedStrings.get(cellStringIndex);
                    cellHandler.handleCell(row, column, str);
                } else {
                    cellHandler.handleCell(row, column, cellValue.toString());
                }
                column = null;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (row != null && column != null) {
                if (cellValue == null) {
                    cellStringIndex = Integer.valueOf(new String(ch, start, length));
                } else {
                    cellValue.append(ch, start, length);
                }
            }
        }
    }
}
