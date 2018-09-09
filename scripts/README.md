# Scripts

This folder contains scripts to unzip and link the project on unix systems.

## Scripts

* `deploy-app dist.zip` unzips a zip file created by `sbt dist`, into a folder with a date and a name on it. E.g. `app-0203`.
* `link-app app-0203` links the passed app to be `app-current`.
* `start-current` starts the app in `app-current`
* `start-current-evolutions` same as above, but allows evolutions.

