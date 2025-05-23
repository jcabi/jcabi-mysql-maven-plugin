<img src="https://www.jcabi.com/logo-square.svg" width="64px" height="64px" />

[![EO principles respected here](https://www.elegantobjects.org/badge.svg)](https://www.elegantobjects.org)
[![DevOps By Rultor.com](https://www.rultor.com/b/jcabi/jcabi-mysql-maven-plugin)](https://www.rultor.com/p/jcabi/jcabi-mysql-maven-plugin)
[![We recommend IntelliJ IDEA](https://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

[![mvn](https://github.com/jcabi/jcabi-mysql-maven-plugin/actions/workflows/mvn.yml/badge.svg)](https://github.com/jcabi/jcabi-mysql-maven-plugin/actions/workflows/mvn.yml)
[![PDD status](https://www.0pdd.com/svg?name=jcabi/jcabi-mysql-maven-plugin)](https://www.0pdd.com/p?name=jcabi/jcabi-mysql-maven-plugin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-mysql-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-mysql-maven-plugin)
[![Javadoc](https://javadoc.io/badge/com.jcabi/jcabi-mysql-maven-plugin.svg)](https://www.javadoc.io/doc/com.jcabi/jcabi-mysql-maven-plugin)

More details are here: [mysql.jcabi.com](http://mysql.jcabi.com/index.html)

Read [this article](http://www.yegor256.com/2014/05/21/mysql-maven-plugin.html),
it explains what this plugin is for.

On Linux, don't forget to install `libaio1` and `libnuma1`:

```
$ sudo apt-get install libaio1 libnuma1
```

## How to contribute?

Fork the repository, make changes, submit a pull request.
We promise to review your changes same day and apply to
the `master` branch, if they look correct.

Please run Maven build before submitting a pull request:

```
$ mvn clean install -Pqulice
```

Make sure you're using JDK 6+ and Maven 3.2+. Older versions won't work.
Also, on Linux, don't forget to install `libaio1` as explained above.
