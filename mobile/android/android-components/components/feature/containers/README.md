# [Android Components](../../../README.md) > Feature > Containers

Feature component for working with contextual identities also known as containers.

The containers are stored with the application-services `fxcontainers` component, which Firefox Desktop uses as well.
`DefaultContainerRepository` seeds the default Personal, Work, Banking and Shopping containers on first use, and
`ContainerMiddleware` mirrors the containers into `BrowserState.containers`.

## Usage

### Setting up the dependency

Use Gradle to download the library from [maven.mozilla.org](https://maven.mozilla.org/) ([Setup repository](../../../README.md#maven-repository)):

```Groovy
implementation "org.mozilla.components:feature-containers:{latest-version}"
```

## License

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/
