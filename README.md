# JIRA Command Line Interface

## Usage

Initialization and command chaining:

    $ java -jar jiracli.jar -c
    jiracli> read | print ${summary}/${status.name}
    End the input with a single .
    JRA-123
    .
    Base URL: https://jira.atlassian.com
    New Report: Voted Issues/Closed
    jiracli> exit

JavaScript evaluation: 

    jiracli> issues JRA-123 | js "forEach.call(input, function(issue) { println(issue.fields.issuetype.name); })"
    Suggestion

    jiracli> js "webService.getIssue('JRA-123')"
    JRA-123

Search and issue filtering:

    jiracli> s "project=JRA and text~'new report'" | filter -f summary '^New report' | p
    JRA-2020 - New reports tab + report

Issue browsing:

    jiracli> issues JRA-123 | browse -n
    https://jira.atlassian.com/browse/JRA-123

## License

Jiracli is licensed under the Apache License, Version 2.0
