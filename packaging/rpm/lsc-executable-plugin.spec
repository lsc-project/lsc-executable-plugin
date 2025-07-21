#=================================================
# Specification file for LSC Executable plugin
#
# Install LSC Executable plugin
#
# BSD License
#
# Copyright (c) 2009 - 2021 LSC Project
#=================================================

%define lsc_min_version		2.2
%define lsc_user		lsc
%define lsc_group		lsc

Name: lsc-executable-plugin
Version: 1.3
Release: 1%{?dist}
Summary: LSC Executable plugin
License: BSD-3-Clause
URL: https://lsc-project.org

Source: %{lsc_executable_name}-%{lsc_executable_version}.jar
Source1: lsc-executable-add-modify-delete-modrdn.pl
Source2: lsc-executable-csv2ldif-get.pl
Source3: lsc-executable-csv2ldif-list.pl

BuildArch: noarch
Requires: lsc >= %{lsc_min_version}

%description
This is an Executable plugin for LSC.

%prep

%build

%install

rm -rf %{buildroot}

# Create directories
mkdir -p %{buildroot}/usr/%{_lib}/lsc
mkdir -p %{buildroot}/var/lib/lsc

# Copy files
cp -a %{SOURCE0} %{buildroot}/usr/%{_lib}/lsc
cp -a %{SOURCE1} %{buildroot}/var/lib/lsc
cp -a %{SOURCE2} %{buildroot}/var/lib/lsc
cp -a %{SOURCE3} %{buildroot}/var/lib/lsc

%post

/bin/chown -R %{lsc_user}:%{lsc_group} /usr/%{_lib}/lsc 
/bin/chown -R %{lsc_user}:%{lsc_group} /var/lib/lsc 


%postun


%files
/usr/%{_lib}/lsc/lsc-executable-plugin*
/var/lib/lsc/lsc-executable*

%changelog
* Mon Jul 21 2025 - Clement Oudot <clem@lsc-project.org> - 1.3-1
- Upgrade to 1.3
- fix value comparison + add unit test for executableLdifDestinationService task

* Mon Apr 14 2025 - Clement Oudot <clem@lsc-project.org> - 1.2-1
- Upgrade to 1.2

* Thu Jan 07 2021 - Clement Oudot <clem@lsc-project.org> - 1.1-0
- Upgrade to 1.1

* Tue Mar 04 2014 - Clement Oudot <clem@lsc-project.org> - 1.0-0
- First package for LSC Executable plugin
