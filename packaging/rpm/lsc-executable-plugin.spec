#=================================================
# Specification file for LSC Executable plugin
#
# Install LSC Executable plugin
#
# BSD License
#
# Copyright (c) 2009 - 2021 LSC Project
#=================================================
%global lsc_min_version		2.2
%bcond_without tests

Name: lsc-executable-plugin
Version: 1.3
Release: 2%{?dist}
Summary: LSC Executable plugin
License: BSD-3-Clause
URL: https://lsc-project.org
Source0: https://github.com/lsc-project/%{name}/archive/v%{version}/%{name}-%{version}.tar.gz
BuildArch: noarch
BuildRequires: maven
BuildRequires: java-devel >= 1:1.6.0
BuildRequires: javapackages-local
BuildRequires: jpackage-utils
BuildRequires: perl-generators
Requires: lsc >= %{lsc_min_version}


%description
This is an Executable plugin for LSC.


%prep
%setup -q


%build
mvn %{!?with_tests:"-Dmaven.test.skip=true"} package


%install
# Jar
mkdir -p %{buildroot}%{_libdir}/lsc
install -m 0644 target/%{name}-%{version}.jar \
  %{buildroot}%{_libdir}/lsc

# Scripts
mkdir -p %{buildroot}%{_localstatedir}/lib/lsc
install -m 0755 scripts/lsc-executable*.pl \
  %{buildroot}%{_localstatedir}/lib/lsc


%files
%license LICENSE.txt
%doc README.md doc/
%{_libdir}/lsc/lsc-executable-plugin*
%{_localstatedir}/lib/lsc/lsc-executable*.pl


%changelog
* Mon Jul 21 2025 Xavier Bachelot <xavier.bachelot@worteks.com> - 1.3-2
- Rework specfile

* Mon Jul 21 2025 - Clement Oudot <clem@lsc-project.org> - 1.3-1
- Upgrade to 1.3
- fix value comparison + add unit test for executableLdifDestinationService task

* Mon Apr 14 2025 - Clement Oudot <clem@lsc-project.org> - 1.2-1
- Upgrade to 1.2

* Thu Jan 07 2021 - Clement Oudot <clem@lsc-project.org> - 1.1-0
- Upgrade to 1.1

* Tue Mar 04 2014 - Clement Oudot <clem@lsc-project.org> - 1.0-0
- First package for LSC Executable plugin
