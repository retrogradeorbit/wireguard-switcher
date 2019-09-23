# wireguard-switcher

Linux system tray application to easily activate/deactivate wireguard endpoints with a popup menu.

## Build

    $ make

## Usage

    $ ./wireguard-switcher

## Options

    $ ./wireguard-switcher --help
    -h, --help     Print the command line help
    -v, --version  Print the version string and exit

## Installation

Copy the `wireguard-switcher` binary to your system path

     $ cp wireguard-switcher /usr/local/bin/

If you want it to startup on your desktop login, make a `~/.config/autostart/wireguard-switcher.desktop`. In it put the following content:

   $ cat ~/.config/autostart/wireguard-switcher.desktop
   [Desktop Entry]
   Version=0.1
   Name=Wireguard Switcher
   Comment=Wireguard Switcher Taskbar App
   Exec=/usr/local/bin/wireguard-switcher
   Terminal=false
   Type=Application
   Categories=Utility;Application;

Then logout and log back in again

## Permissions

The user running the `wireguard-switcher` will need passwordless `sudo` access to the following commands:

- sudo wg
- sudo service wg-quick@connection start
- sudo service wg-quick@connection stop

where `connection` is the name of the connection

You can give a user complete root sudo access to _every_ command by appending the following to the bottom of `/etc/sudoers`:

    the_username ALL=(root) NOPASSWD: ALL

Or if you only want to grant access to _only the above commands_ you can append:

    the_username ALL=(root) NOPASSWD: /usr/bin/wg, /usr/sbin/service wg-quick@*

## Operation

On startup, the systray application reads the contents of the `/etc/wireguard` directory. For each file it finds matching `*.conf` is makes a menu entry for that connection.

## Bugs

GraalVM's native-image presently doesn't work for swing/awt apps. See: https://github.com/oracle/graal/issues/1327

So I am using `lein-bin` to build a single executable for distribution. This requires that the machine you are installing the application on have a working Java JVM installed.

## License

Copyright © 2019 Crispin Wellington

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
