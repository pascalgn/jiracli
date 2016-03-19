# JIRA Command Line Interface

## Usage

Initialization and command chaining: 

    $ java -jar jiracli.jar -c
    Root URL: https://jira.atlassian.com/
    Username:
    jiracli> read | print ${summary}/${status.name}
    JRA-123
    New Report: Voted Issues/Closed
    
    jiracli> exit

JavaScript evaluation: 

    jiracli> get JRA-123 | js "forEach.call(input, function(issue) { println(issue.fields.issuetype.name); })"
    Suggestion
    jiracli> exit

## License

Jiracli is licensed under the Apache License, Version 2.0
