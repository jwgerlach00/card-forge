# Pulling Upstream Changes from Card Forge

This repository is a detached "fork" from Card Forge (https://github.com/Card-Forge/forge).

This document outlines how to integrate upgrades from the upstream Card Forge repository.

1. Add the Card Forge upstream: `git remote add upstream https://github.com/Card-Forge/forge`

2. Verify that the upsteam has been added: `git remote -v`
    
    example output:
    ```
    origin  https://github.com/jwgerlach00/card-forge.git (fetch)
    origin  https://github.com/jwgerlach00/card-forge.git (push)
    upstream        https://github.com/Card-Forge/forge (fetch)
    upstream        https://github.com/Card-Forge/forge (push)
    ```

3. Fetch upstream changes: `git fetch upstream`

4. Create a branch for upstream changes: `git branch <branch-name>`

5. Checkout branch: `git checkout <branch-name>`

6. Merge upstream master/main (Card Forge uses master) branch to <branch-name>: `git merge upstream/master`

7. Push changes to <branch-name> using CLI or UI.

8. Open PR into jwgerlach00/card-forge master branch using GitHub.com.