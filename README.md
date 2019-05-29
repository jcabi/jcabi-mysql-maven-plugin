<img src="http://img.jcabi.com/logo-square.png" width="64px" height="64px" />

[![EO principles respected here](https://www.elegantobjects.org/badge.svg)](https://www.elegantobjects.org)
[![Managed by Zerocracy](https://www.0crat.com/badge/C3RUBL5H9.svg)](https://www.0crat.com/p/C3RUBL5H9)
[![DevOps By Rultor.com](http://www.rultor.com/b/jcabi/jcabi-mysql-maven-plugin)](http://www.rultor.com/p/jcabi/jcabi-mysql-maven-plugin)
[![We recommend IntelliJ IDEA](https://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

[![Build Status](https://travis-ci.org/jcabi/jcabi-mysql-maven-plugin.svg?branch=master)](https://travis-ci.org/jcabi/jcabi-mysql-maven-plugin)
[![PDD status](http://www.0pdd.com/svg?name=jcabi/jcabi-mysql-maven-plugin)](http://www.0pdd.com/p?name=jcabi/jcabi-mysql-maven-plugin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-mysql-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-mysql-maven-plugin)
[![Javadoc](https://javadoc.io/badge/com.jcabi/jcabi-mysql-maven-plugin.svg)](http://www.javadoc.io/doc/com.jcabi/jcabi-mysql-maven-plugin)
[![Build status](https://ci.appveyor.com/api/projects/status/3axyj0ho4xjc5i5k)](https://ci.appveyor.com/project/yegor256/jcabi-mysql-maven-plugin)

More details are here: [mysql.jcabi.com](http://mysql.jcabi.com/index.html)

Read [this article](http://www.yegor256.com/2014/05/21/mysql-maven-plugin.html),
it explains what this plugin is for.

On Linux, don't forget to install `libaio1`:

```
$ sudo apt-get install libaio1
```

## Questions?

If you have any questions about the framework, or something doesn't work as expected,
please [submit an issue here](https://github.com/jcabi/jcabi-mysql-maven-plugin/issues/new).

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
