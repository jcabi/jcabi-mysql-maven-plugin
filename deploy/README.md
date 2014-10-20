In order to deploy a new version of mysql-dist artifact
to Maven Central you should do the following steps:

 1. Update `deploy/pom.xml` file with a new version of MySQL

 2. Download five files from http://dev.mysql.com/downloads/mysql/

   1. Windows 64
   2. Windows 32
   3. MacOS
   4. Linux
   5. Linux

 3. Unpack them all

 4. Zip each of them with the following command:

```
$ cd directory_with_dist_content
$ zip -0 -r ../dist-1.zip *
```

 5. Install it locally and test how it works with the plugin

```
$ mvn install:install-file -Dfile=dist-1.zip -DgroupId=com.jcabi \
    -DartifactId=mysql-dist -Dversion=5.6.21 -Dpackaging=zip -Dclassifier=mac-x86_64
```

 6. Sign them all, using gnupg:

```
$ gpg -ab pom.xml
$ gpg -ab dist-1.zip
$ gpg -ab dist-2.zip
$ gpg -ab dist-3.zip
$ gpg -ab dist-4.zip
$ gpg -ab dist-5.zip
```

 5. Login to sonatype and deploy them both (pom.xml and all dist-*.zip). You
 should have these files before upload:

```
pom.xml
pom.pom.asc
dist-1.zip
dist-1.zip.asc
dist-2.zip
dist-2.zip.asc
dist-3.zip
dist-3.zip.asc
dist-4.zip
dist-4.zip.asc
dist-5.zip
dist-6.zip.asc
```
