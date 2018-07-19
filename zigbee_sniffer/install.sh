#!/bin/sh

echo "ANGRY GENESIS INSTALLER"
echo "***********************"
echo ""

INSTALL_DIR="/opt"

main() {
	cd chmod -R 0777 $INSTALL_DIR
    install_deps
    install_libmich
    install_ag
    setup_service
    echo "[+] Install complete"
}

install_deps() {
	echo "[+] Updating Raspian OS"
	sudo apt update 
    sudo apt dist-upgrade -y
	echo "[+] Installing Dependiencies"
	sudo apt install -y git
	sudo apt install -y python-pip
	sudo -H pip install -y libusbl
	sudo apt install -y openjdk-8-jre-headless

}

install_libmich() {
	cd $INSTALL_DIR
	echo "[+] Downloading and installing librarys from Github"
	sudo git clone https://github.com/mitshell/libmich.git
	cd libmich
	sudo python setup.py install
}

install_ag() {
	cd $INSTALL_DIR
	echo "[+] Getting ANGRY GENESIS from Github"
	sudo git clone https://github.com/bnt2025/AngryGenesis.git
	echo "[+] Starting ANGRY GENESIS"
	cd AngryGenesis

}

setup_service() {
	sudo cp $INSTALL_DIR/AngryGenesis/docs/angrygenesis.service /etc/systemd/system/
	sudo chmod 0777 /etc/systemd/system/angrygenesis.service
	sudo systemctl daemon-reload
	sudo systemctl enable angrygenesis.service

}

main
