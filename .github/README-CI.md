# CI/CD for Slang Unofficial LSP

This project uses GitHub Actions for continuous integration and delivery with automated versioning.

## CI/CD Workflow

The CI/CD process is handled by a single workflow file (`ci.yml`) with two jobs:

## Jobs and Triggers

### Build Job

**Triggers:**
- Pull requests to `main`
- Pushes to `main`

**Actions:**
1. Builds the plugin
2. Runs tests
3. Uploads artifacts
4. Comments on PR with build status (for PRs)

### Publish Job

**Triggers:**
- Pushes to `main` (i.e., when a PR is merged)

**Actions:**
1. Automatically increments the patch version in `version.properties`
2. Builds and signs the plugin
3. Publishes to JetBrains Marketplace
4. Creates a GitHub release with artifacts
5. Commits the version change back to the repository

## Setting up GitHub Secrets

To enable plugin signing and publishing, add these secrets to your repository:

1. Go to Settings → Secrets and variables → Actions
2. Add the following secrets:
   - `SLANG_LSP_CERTIFICATE_CHAIN`: Your plugin signing certificate chain
   - `SLANG_LSP_PRIVATE_KEY`: Your private key for signing
   - `SLANG_LSP_PRIVATE_KEY_PASSWORD`: Password for the private key
   - `SLANG_LSP_PUBLISH_TOKEN`: Token for publishing to JetBrains Marketplace

## Version Management

The project uses a `version.properties` file with the format:
```properties
major=0
minor=0
patch=6
suffix=
```

Each time a PR is merged to main, the patch version is automatically incremented, and a new release is created.

## Release Process

1. Create a PR to main with your changes
2. When the PR is merged:
   - Version is automatically bumped (patch level)
   - A new GitHub release is created
   - Plugin is built, signed, and published to JetBrains Marketplace

For major or minor version changes, manually edit the `version.properties` file as part of your PR.
