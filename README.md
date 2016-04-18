# Jiracli

Jiracli is a command line interface for Jira to automate common tasks and perform advanced bulk changes.

* [Examples](#examples)
* [Usage](#usage)
* [Model](#model)
* [License](#license)

## Examples

Initialization and command chaining:

    $ java -jar jiracli.jar -c
    jiracli> issues JRA-123 | print '${summary} [${status.name}]'
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

Jiracli can be started in one of two modes, GUI and console mode. When no explicit mode is given
(`-c` for console mode or `-g` for GUI mode), a mode is selected automatically.

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

For more information about objects, see the section [Model](#model).

### Commands

This section describes some of the basic commands. To get a list of all commands, type `help`.

#### Load issues

* `issues` can be used to load specific issues (`issues JRA-1 JRA-7 JRA-123`)
  or to find issues belonging to the input object (`boards | sprints | issues`)
* `read` reads issues from a given file (`read my-issues.txt`)
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

### Caching

All requests will be cached during a session, to improve response times and reduce server load.
Use `cache -c` to clear all current cache entries.

## Model

Jiracli uses different objects to represent the corresponding Jira elements. The properties of each object can be accessed by the `get` or `print` command.

### Issue

Issues are one of the key concepts in Jira and can represent bugs, tasks, tickets or anything else depending on the project requirements. Issues are used by almost all commands and are usually accessed by using the `issues`, `search` or `read` command.

#### Properties

- `key`: The unique issue key, e.g. `JIR-123`

### Project

Projects are used to organize and group issues. Each issue belongs to one project.

#### Properties

- `id`: The unique identifier of the project
- `key`: The project key, e.g. `PROJ`. Issues of this project will be prefixed with the key (`PROJ-1`, `PROJ-2`, etc.)
- `name`: The user-defined name, e.g. `Example Project`

### Field

Every issue has a set of system-defined fields, depending on the issue type.
Additionally, administrators can define custom fields for issues, to match the respective project requirements.

Each field has a technical identifier (`id`) and a user-defined name. Usually, both id and name are accepted as command arguments.

#### Properties

- `id`: The technical identifier of the field, e.g. `status`.
  Identifiers for custom fields have the format `custom_12345`.

### Attachment

Attachments represent file attachments of issues. The `download` command can be used to access attachments.

#### Properties

- `id`: The technical numerical identifier of the attachment
- `filename`: The original filename, e.g. `report.pdf`
- `mimeType`: The file's mime type, e.g. `application/pdf`
- `size`: The file size, in bytes
- `content`: The URL to the file contents, e.g. `http://example.com/secure/attachment/12345/report.pdf`

### Board

Boards are used to organize and display issues of one or more projects.

Boards are only available when the Jira Agile add-on is installed in the Jira instance.

#### Properties

- `id`: The unique identifier of the board
- `name`: The user-defined name, e.g. `A simple Jira-Board`
- `type`: The type of the board: Scrum, Kanban or Unknown

### Sprint

Sprints are parts of Scrum boards and are used to track the progress of issues over a period of time.

Sprints are only available when the Jira Agile add-on is installed in the Jira instance.

#### Properties

- `id`: The unique identifier of the sprint
- `name`: The user-defined name, e.g. `Sprint 17`
- `state`: The state of the sprint: Closed, Active, Future or Unknown

## License

Jiracli is licensed under the Apache License, Version 2.0
