#=================================================
# Specification file for LSC Executable plugin
#
# Install LSC Executable plugin
#
# BSD License
#
# Copyright (c) 2009 - 2014 LSC Project
#=================================================

#=================================================
# Variables
#=================================================
%define lsc_executable_name	lsc-executable-plugin
%define lsc_executabme_version	1.0
%define lsc_min_version		2.1
%define lsc_user		lsc
%define lsc_group		lsc

#=================================================
# Header
#=================================================
Summary: LSC Executable plugin
Name: %{lsc_executable_name}
Version: %{lsc_executable_version}
Release: 0%{?dist}
License: BSD
BuildArch: noarch

Group: Applications/System
URL: http://lsc-project.org

Source: %{lsc_executable_name}-%{lsc_executable_version}.jar
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

Requires(pre): coreutils
Requires: lsc >= %{lsc_min_version}

%description
This is an Executable plugin for LSC

%prep

%build

%install

rm -rf %{buildroot}

# Create directories
mkdir -p %{buildroot}/usr/%{_lib}/lsc

# Copy files
cp -a %{SOURCE0} %{buildroot}/usr/%{_lib}/lsc

%post

/bin/chown -R %{lsc_user}:%{lsc_group} /usr/%{_lib}/lsc 


%postun

%clean
rm -rf %{buildroot}

%files
%defattr(-, root, root, 0755)
/usr/%{_lib}/lsc/lsc-executable-plugin*

#=================================================
# Changelog
#=================================================
%changelog
* Tue Mar 04 2014 - Clement Oudot <clem@lsc-project.org> - 1.0-0
- First package for LSC Executable plugin
