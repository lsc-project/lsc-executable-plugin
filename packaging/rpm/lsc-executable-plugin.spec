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

Name: lsc-executable-plugin
Version: 1.4
Release: 1%{?dist}
Summary: LSC Executable plugin
License: BSD-3-Clause
URL: https://lsc-project.org
Source0: lsc-executable-plugin-1.4.jar
Source1: lsc-executable-add-modify-delete-modrdn.pl
Source2: lsc-executable-csv2ldif-get.pl
Source3: lsc-executable-csv2ldif-list.pl
BuildArch: noarch
BuildRequires: coreutils
BuildRequires: perl-generators
Requires: lsc >= %{lsc_min_version}


%description
This is an Executable plugin for LSC.


%prep


%install

# Create directories
mkdir -p %{buildroot}/usr/%{_lib}/lsc
mkdir -p %{buildroot}%{_docdir}/%{name}/scripts

# Copy files
cp -a %{SOURCE0} %{buildroot}%{_libdir}/lsc
cp -a %{SOURCE1} %{buildroot}%{_docdir}/%{name}/scripts/
cp -a %{SOURCE2} %{buildroot}%{_docdir}/%{name}/scripts/
cp -a %{SOURCE3} %{buildroot}%{_docdir}/%{name}/scripts/


%files
%{_libdir}/lsc/lsc-executable-plugin*
%doc %{_docdir}/%{name}/scripts


%changelog
* Mon Jul 27 2026 David Coutadeur <david.coutadeur@gmail.com> - 1.4-1
- Upgrade to 1.4
- Clean rpm specfile
- Properly handle Base64-encoded attributes
- Compatibility with LSC 2.3

* Mon Jul 21 2025 - Clement Oudot <clem@lsc-project.org> - 1.3-1
- Upgrade to 1.3
- fix value comparison + add unit test for executableLdifDestinationService task

* Mon Apr 14 2025 - Clement Oudot <clem@lsc-project.org> - 1.2-1
- Upgrade to 1.2

* Thu Jan 07 2021 - Clement Oudot <clem@lsc-project.org> - 1.1-0
- Upgrade to 1.1

* Tue Mar 04 2014 - Clement Oudot <clem@lsc-project.org> - 1.0-0
- First package for LSC Executable plugin
