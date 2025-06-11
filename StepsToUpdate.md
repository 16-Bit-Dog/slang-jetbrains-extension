# Update and Release Process

## Automated Process

1. Change code and push to a feature branch
2. Create a pull request to `main`
3. When the PR is merged:
   - Patch version is automatically incremented in `version.properties`
   - A new GitHub release is created
   - Plugin is built, signed, and published to JetBrains Marketplace

## Manual Version Updates

For major or minor version changes, include the version change in your PR by editing the `version.properties` file directly.

## Manual Build Process

1. Change code
2. Build locally with `./gradlew buildPlugin`
3. Find the plugin in `build/distributions`

See [CI Documentation](.github/README-CI.md) for more details.