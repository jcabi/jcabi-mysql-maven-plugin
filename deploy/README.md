In order to deploy a new version of mysql-dist artifact to Maven Central you should do the following steps:

Update `deploy/pom.xml` file with a new version of MySQL:

```
$ mvn versions:set -DnewVersion=8.0.33.1 --batch-mode
```

Download five files from http://dev.mysql.com/downloads/mysql/ and repackage them into ZIP:

  * mac-x86_64.zip
  * mac-aarch64.zip
  * linux-aarch64.zip
  * linux-x86_64.zip
  * windows-x86.zip

You can package them like this:

```
$ cd directory_with_dist_content
$ zip -r ../linux-amd64.zip *
```

Install it locally and test how it works with the plugin (not from the `deploy` directory):

```
$ mvn install:install-file -Dfile=mac-x86_64.zip -DgroupId=com.jcabi -DartifactId=mysql-dist -Dversion=5.6.21 -Dpackaging=zip -Dclassifier=mac-x86_64
```

Deploy to Sonatype:

```
$ mvn deploy
```

Make sure, you have a `<server>` in you `~/.m2/settings.xml` with `id` set to `oss.sonatype.org`, like this:

```xml
<settings>
  <servers>
    <server>
      <id>oss.sonatype.org</id>
      <username>...</username>
      <password>...</password>
    </server>
  </servers>
</settings>
```
