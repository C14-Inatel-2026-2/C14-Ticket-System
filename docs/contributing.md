Git Workflow

For the **Contributors**

The main branch is protected, which means that no commit can be made directly.
In order to implement new functionalities in the main, please open a Pull Request in another branch.

The workflow needs to be like:

1 - git pull origin main                                    ### Pull the latest change in the main branch
2 - git checkout -b <objective>/<name>                      ### Switch to a new branch
3 - git add <changed_files | new_files | deleted_files>     ### Add the changed files to stage
4 - git commit -m "<description>"                           ### Commit the changes with "ideally" a good comment
5 - git push origin <branch_name>                           ### Push the changes to the new branch

*Important*
All java files needs a Javadoc with the @author label.
Without it the project cannot be built.

*Good Practice*
Name the branch with a <objective> tag, where:
feature : for new functionalities
fix     : for error fixing
chore   : for configuration (it doesnt add a new functionality, just change a configuration of some kind)
docs    : for exclusivily changing/updating the documents
tests   : for implementing/fixing tests

Example:
main
 |
 |- feature/create-ticket
 |- feature/login
 |- fix/ticket-validation
 |- chore/database-config
 |- doc/contributors
 | -tests/ticket-controller


