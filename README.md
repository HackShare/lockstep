lockstep
========

This is a project to explore the replication of resources, such as files in a distributed system using the concept of a shared system memory, which can be implemented using services like ZooKeeper or FireBase, or in cases where speed is not so significant, a simple database.  The initial focus is to build a synchronization client for synchronizing files in two directories that can be on different machines.

optimization goals
========

. Fast discovery of changes - prioritize change discovery by reducing our primary activity to those messaging required for change discovery
. Ability to handle large files - minimize load on system as more large files are added.  Unnecessary copying of files and parallelizing of file copy between machines may yield some nice results.
. Quick startup - when we have a lot of files to sync to an empty local directory, we don't want to be waiting on that sync to finish before we can start pushing changes

developer
========

If you are looking at this as a developer, this is an eclipse Indigo project and currently does not count on java features beyond Java 5.  

Please start in the tests directory.  It is being coded using TDD, so expect lots of refactoring as the solution materializes.

This code is currently not ready to release.

