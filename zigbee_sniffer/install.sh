clear
echo "ANGRY GENESIS INSTALLER"
echo "***********************"
echo ""
echo "[+] Updating Raspian OS"
sudo apt update && upgrade && dist-upgrade -y
echo "[+] Installing Dependiencies"
sudo apt install git
sudo apt install python-pip
sudo pip install libusbl
sudo apt install openjdk-8-jre-headless
echo "[+] Downloading and installing librarys from Github"
sudo git clone https://github.com/mitshell/libmich.git
cd libmich
sudo python setup.py install
cd ..
echo "[+] Getting ANGRY GENESIS from Github"
sudo git clone https://github.com/bnt2025/AngryGenesis.git
echo "[+] Starting ANGRY GENESIS"
sudo python /zigbee_sniffer/sniffer.py

