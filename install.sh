#!/bin/sh

echo "ANGRY GENESIS INSTALLER"
echo "***********************"
echo ""

INSTALL_DIR="/opt"

main() {
    cd $INSTALL_DIR
    sudo chmod -R 0777 $INSTALL_DIR
    install_deps
    install_ag
    install_libmich
    setup_service
    echo "[+] Install complete"
}

install_deps() {
	echo "[+] Updating Raspian OS"
	sudo apt update 
	sudo apt dist-upgrade -y
	echo "[+] Installing Dependiencies"
	sudo apt install -y python-pip
	sudo -H pip install libusb1
	sudo apt install -y openjdk-8-jre-headless
	sudo apt install -y gpsd gpsd-clients python-gps
	sudo apt install -y usbmount

}

install_ag() {
	mv ../AngryGenesis $INSTALL_DIR/

}

install_libmich() {
	# TODO have a test for this and install if needed. 
	cd $INSTALL_DIR
	echo "[+] Downloading and installing librarys from Github"
	sudo git clone https://github.com/mitshell/libmich.git
	cd libmich
	sudo python setup.py install
}


setup_service() {
	sudo cp $INSTALL_DIR/AngryGenesis/docs/angrygenesis.service /etc/systemd/system/
	sudo chmod 0777 /etc/systemd/system/angrygenesis.service
	sudo systemctl daemon-reload
	sudo systemctl enable angrygenesis.service

}

configure_gpsd() {
	FILE="/etc/default/gpsd"
	/bin/cat <<EOM >$FILE
	START_DAEMON="true"
	USBAUTO="true"
	DEVICES="/dev/ttyUSB0"
	GPSD_OPTIONS="-n"
EOM
}

main
