# Jiracli

Jiracli is a command line interface for Jira to automate common tasks and perform advanced bulk changes.

* [Examples](#examples)
* [Usage](#usage)
* [License](#license)

## Examples

Initialization and command chaining:

    $ java -jar jiracli.jar -c
    jiracli> issues JRA-123 JRA-321 | filter -f key 'JRA-123' | print '${summary} [${status.name}]'
    Base URL: https://jira.atlassian.com
    New Report: Voted Issues [Closed]
    jiracli> exit

Search and issue filtering:

    jiracli> search "project=JRA and text~'new report'" | filter -e -f summary '^New report' | print
    JRA-2020 - New reports tab + report

JavaScript evaluation: 

    jiracli> issues JRA-123 | js -l "forEach.call(input, function(issue) { println(issue.fields.issuetype.name); })"
    Suggestion

    jiracli> js "webService.getIssue('JRA-123')" | parse
    JRA-123

    jiracli> echo "Hello\nWorld\nHello, world!" | split | filter -j "/^Hello/.test(input)"
    Hello
    Hello, world!

Issue browsing:

    jiracli> issues JRA-123 | browse -n
    https://jira.atlassian.com/browse/JRA-123

## Usage

Jiracli can be started in one of two modes, GUI and console mode.
After startup, Jiracli presents a command prompt to execute commands.
For information about available commands, see the section [Commands](#commands).

Almost all commands accept positional arguments, indicated by angle brackets in the help text:

    jiracli> echo -h
    usage: echo [-h] [--] [<text>...]

Optional arguments for commands are prefixed with a single or double dash.
Usually, both short (`-s`) and long forms (`--long`) can be used.

A double dash can be used to separate positional from optional arguments,
when necessary (`echo -- -h`).

Commands can be chained by using the pipe symbol (`cmd1 | cmd2`),
passing the output of the first command to the second command.
Unlike traditional Unix shells, Jiracli passes objects instead
of plain text between commands. This allows chaining of multiple
modifications, for example:

    jiracli> issues JRA-123 | set summary 'Hello' | set description 'World' | update

#### Authentication
Jiracli may require you to grant it access permission to some of your Jira board.
To achieve it, you need to create API token for Jiracli by the following steps:
- Login in to https://id.atlassian.com/manage/api-tokens, create your API token and keep it.
- When Jiracli asks for password when using command, paste you API token there.
```
Please enter the credentials for https://jira.atlassian.net
Username: your_jira_account_email@mail.com
Password: [ENTER_YOUR_API_TOKEN_HERE]
```


### Commands

This section describes some of the basic commands. To get a list of all commands, type `help`.

#### Load issues

* `issues` can be used to load specific issues (`issues JRA-1 JRA-7 JRA-123`)
  or to find issues belonging to the input object (`boards | sprints | issues`)
* `read` reads text from a given file (`read my-issues.txt | parse -k`)
* `search` executes the given JQL script and returns the issues (`search 'project = JRA and issuetype = Epic'`)

#### Relationships

* `links` displays all issues linked to another issue (`issues JRA-1 | links`)

#### Transformations

* `sort` changes the order of the input list (`search 'summary ~ bug' | sort -f priority`)
* `filter` returns only the matching items of a list (`sprints -s 10123 | issues | filter -f issuetype Task`)
* `head` limits the output to the first 10 (by default) items (`search 'assignee = currentUser()' | head`)

#### Issue modifications

* `set` sets the given field to the given value (`issues JRA-123 | set summary Test | print`).
  However, when using `set`, `update` must be called to actually send the modifications
  to the server (`issues JRA-123 | set summary Test | update`)
* `labels` can be used to add or remove labels from the given issues. Like `set`, the
  changes will only be permanent after calling `update` (`search 'summary ~ bug' | labels -a Bug | update`)
* `edit` opens the issues in an external editor, so that multiple issues may be edited at once.
  Like `set`, the changes will only be permanent after calling `update` (`issues JRA-234 | edit | update`)
* `transition` can be used to change the status of issues. The issue's workflow is used
  to determine a path from the current status to the target status. If multiple
  paths are found, the shortest is used (`issues JRA-123 | transition -n Closed`)
* `link` creates a relationship between the input and the given issue (`issues JRA-101 | link JRA-202 Blocks`)

#### Output

* `print` prints formatted output and can display properties (`issues JRA-1 | print $issuetype.name`)
* `get` has an optional parameter to display the raw field value (`issues JRA-1 | get -r status`)
* `properties` shows all available properties of the given objects (`projects -p JRA | properties`)

#### Caching

All requests will be cached in memory during a session, to improve response times and reduce server load.
Use `cache -c` to clear all current cache entries.

## License

Jiracli is licensed under the Apache License, Version 2.0
