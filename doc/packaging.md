# Packaging

## Update version

Update version in:
* pom.xml
* packaging/debian/changelog
* packaging/rpm/lsc-executable-plugin.spec

## Build

## jar file

To build jar:
```
mvn clean package
```

File is available in `target/'.

## Debian

After building jar file, copy `packaging/debian` in `target/`:
```
cp -a packaging/debian target/
```

Then create package:
```
cd target/
dpkg-buildpackage -b -kLSC
```

## RPM

After building jar file, copy it to SOURCES, with other scripts:
```
cp target/lsc-executable-plugin*.jar scripts/* SOURCES/
```

Copy spec file:
```
cp packaging/rpm/lsc-executable-plugin.spec SPECS/
```

Then create package:
```
rpmbuild -ba SPECS/lsc-executable-plugin.spec
rpm --addsign RPMS/noarch/lsc-executable-plugin-*.noarch.rpm
```

