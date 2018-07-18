#!/bin/sh

# Simple installer for the IEE802.15.4 and RFTap GNURadio blocks
# Unfortunatly it uses UHD. I tried modifying it to use LimeSDR-Mini with no avail
# I included them in to grc folder

main() {
	add_repos
	install_deps
	install_grlime
	install_grfoo
	install_grieee80215
	install_rftap
	setup_gnuradio
}

add_repos() {
	sudo add-apt-repository -y ppa:pothosware/framework
	sudo add-apt-repository -y ppa:pothosware/support
	sudo add-apt-repository -y ppa:myriadrf/drivers
}

install_deps() {
	sudo apt install unzip -y
	sudo apt install gnuradio-dev -y
	sudo apt install limesuite liblimesuite-dev limesuite-udev -y
}

install_grlime() {
	git clone https://github.com/myriadrf/gr-limesdr.git
	cd gr-limesdr
	build_src
}

# Have to use the master branch as GNURadio renamed a varible and it fails to compile
install_grfoo() {
	cd /usr/local/src
	wget https://github.com/bastibl/gr-foo/archive/master.zip
	unzip master.zip && rm master.zip
	cd gr-foo-master
	build_src

}

# Have to use the master branch as GNURadio renamed a varible and it fails to compile

install_grieee80215() {
	wget https://github.com/bastibl/gr-ieee802-15-4/archive/master.zip
	unzip master.zip && rm master.zip
	cd gr-ieee802-15-4-master
	build_src
}

install_rftap() {
	git clone https://github.com/rftap/gr-rftap.git
	cd gr-rftap
	build_src
}

build_src() {
	mkdir build
	cmake ../
	make -j4
	sudo make install
	sudo ldconfig
}

setup_gnuradio() {
	sudo chmod 0777 /home/$USER/.grc_gnuradio
	# If you get the cannot find transceiver_OQPSK block this is why 
	echo "[+] Open /usr/local/src/gr-ieee802-15-4-master/examples in GNURadio and build it."
}

main
