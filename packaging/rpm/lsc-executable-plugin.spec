#=================================================
# Specification file for LSC Executable plugin
#
# Install LSC Executable plugin
#
# BSD License
#
# Copyright (c) 2009 - 2021 LSC Project
#=================================================
%global lsc_min_version		2.3

%bcond_with build_from_sources
%bcond_with tests

Name: lsc-executable-plugin
Version: 1.4
Release: 1%{?dist}
Summary: LSC Executable plugin
License: BSD-3-Clause
URL: https://lsc-project.org
%if %{with build_from_sources}
Source0: https://github.com/lsc-project/%{name}/archive/v%{version}/%{name}-%{version}.tar.gz
%else
Source1: https://www.lsc-project.org/archives/lsc-executable-plugin-%{version}.jar
Source2: https://raw.githubusercontent.com/lsc-project/%{name}/refs/tags/v%{version}/scripts/lsc-executable-add-modify-delete-modrdn.pl
Source3: https://raw.githubusercontent.com/lsc-project/%{name}/refs/tags/v%{version}/scripts/lsc-executable-csv2ldif-get.pl
Source4: https://raw.githubusercontent.com/lsc-project/%{name}/refs/tags/v%{version}/scripts/lsc-executable-csv2ldif-list.pl
%endif
BuildArch: noarch

BuildRequires: coreutils
%if %{with build_from_sources}
BuildRequires: jpackage-utils
BuildRequires: java-devel >= 1:21
BuildRequires: maven
BuildRequires: maven-local
%endif
Requires: lsc >= %{lsc_min_version}


%description
This is an Executable plugin for LSC.


%prep
%if %{with build_from_sources}
%setup -q
%endif


%build
%if %{with build_from_sources}
mvn %{!?with_tests:"-Dmaven.test.skip=true"} package
%endif


%install
# Jar
mkdir -p %{buildroot}%{_libdir}/lsc
%if %{with build_from_sources}
install -m 0644 target/%{name}-%{version}.jar \
  %{buildroot}%{_libdir}/lsc
%else
install -m 0644 %{SOURCE1} %{buildroot}%{_libdir}/lsc/
mkdir scripts
install -m 0644 %{SOURCE2} %{SOURCE3} %{SOURCE4} scripts/
%endif


%files
%if %{with build_from_sources}
%license LICENSE.txt
%doc README.md doc/*
%endif
%doc scripts/*
%{_libdir}/lsc/lsc-executable-plugin*


%changelog
* Mon Jul 27 2026 Xavier Bachelot <xavier.bachelot@worteks.com> - 1.4-1
- Update to 1.4
  - Properly handle Base64-encoded attributes
  - Compatibility with LSC 2.3
- Clean rpm specfile

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
