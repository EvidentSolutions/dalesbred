# How to contribute

First of all, thank you for considering contributing!

You can contribute to Dalesbred by [creating issues](https://github.com/EvidentSolutions/dalesbred/issues/new) or
[by submitting pull request](https://github.com/EvidentSolutions/dalesbred/compare/). Here are some guidelines for
contributions.

## Guidelines for bug reports

When submitting bug reports, include the following details if applicable:

  - version of Dalesbred and JDK
  - versions of other libraries if using Dalesbred's integration features
  - database product and version
  - used JDBC driver and important connection settings

If you can produce a standalone example program reproducing the bug, all the better. 

## Guidelines for pull requests

### Tests

Any new features should include tests.

Extensive mocking in tests is discouraged. If you need to database in your tests, use in-memory HSQL (see 
`DatabaseTest` for an example). If you are writing integration for specific database, include tests for that 
database (see e.g. `PostgreSQLLargeObjectTest`).

### Documentation

Public APIs should be documented using JavaDoc. Strive for clarity instead of completeness. There's no reason
to document every parameter or return value separately if they are clear from the context.

For major features it's encouraged to include description and examples in reference documentation.

### Commits

Prefer series of small isolated commits to large commits that do changes all over the codebase. Try to separate
refactoring that prepares for landing a feature from the feature commits themselves.

Please follow [the seven rules of a great git commit message](http://chris.beams.io/posts/git-commit/#seven-rules).
