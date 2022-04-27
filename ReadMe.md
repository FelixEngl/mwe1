# Minimal Working Example: IntelliJ - MPP - Commons-UnresolvedReference-Problem

When importing `kotlinx.<whatever>` in the commonMain (it doesn't matter which subproject), it fails with a 
`Unresolved reference: kotlinx`. The code worked perfectly fine until yesterday.


## Hints
The compiled application does not run because it is a stripped down ktor-project and I didn't bother to clean up whe
whole thing.


