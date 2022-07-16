# distributedGradeBook
Project 2-1: Implementation and Demo

### Getting Set Up ###
1. Create a github account and log in
2. To make your life easier, set up an ssh key using the following guides

    Generate an SSH key
    - https://docs.github.com/en/authentication/connecting-to-github-with-ssh/adding-a-new-ssh-key-to-your-github-account
    
    Copy the public key contents to github
    - I prefer using xclip, you can just open the .pub file and copy it's contents if you like.
    - xclip command `xclip -sel clip < ~/.ssh/id_ed25519.pub`

3. Clone the repo.

    `git clone git@github.com:BFrankC/distributedGradeBook.git`


### Project workflow ###
To be discussed in greater detail at kickoff meeting
#### Suggested Workflow ####
1. Click on the issues Tab
2. Find an issue that is not assigned to someone
3. Assign yourself to that issue 
4. create a new branch for that issue (development tab on right of issue will help with this)
5. git fetch origin
6. git checkout "issueName"
7. Complete work specific to the issue you checkout
8. git add . 
9. git commit -m "short description of what you did"
10. git push
11. create a pull request
11. Go back to step 1



