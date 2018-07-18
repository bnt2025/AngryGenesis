import threading
import time
import gps


class GPSThread(threading.Thread):
    environData = {"GPS Source": "None", 'Latitude': 0, 'Longitude': 0, 'fix': 0, 'connected': 0, 'Altitude': 0, 'speed': 0,
                   'heading': 0, 'last_update_time': 0, 'hdop': 0, 'vdop': 0}
    VIRTUAL_MODE = False

    def __init__(self):
        super(GPSThread, self).__init__()
        self._stop = threading.Event()
        self._stopped = threading.Event()
        self.setDaemon(True)
        self.gpsd = None

    def run(self):
        last_gps_update = time.time()

        while not self._stop.isSet():
            if self.environData["GPS Source"] in "GPSD":
                try:
                    # starting the stream of info
                    self.gpsd = gps.gps(mode=gps.WATCH_NEWSTYLE)
                    while not self._stop.isSet():
                        self.gpsd.next()
                        gps_status = str(self.gpsd.fix.mode)
                        self.environData['fix'] = gps_status
                        # print("GPS fix: " + gps_status)
                        # Get the time to check how long it has been since the last GPS update.
                        time_now = time.time()
                        elapsed = time_now - last_gps_update
                        # print(elapsed)

                        # Only update on 2D or 3D fixes OR of the time since a GPS fix is more than 5 seconds
                        if ("2" in gps_status) or ("3" in gps_status):
                            last_gps_update = time.time()
                            self.environData['Latitude'] = self.gpsd.fix.latitude
                            self.environData['Longitude'] = self.gpsd.fix.longitude
                            # self.environData['connected'] = str(self.gpsd.fix.)
                            self.environData['Altitude'] = self.gpsd.fix.altitude
                            self.environData['last_update_time'] = time_now
                            self.environData['hdop'] = self.gpsd.fix.epx
                            self.environData['vdop'] = self.gpsd.fix.epy
                            self.environData['speed'] = self.gpsd.fix.speed
                            self.environData['heading'] = str(
                                self.gpsd.fix.track)
                        time.sleep(0.1)

                        if not self.environData["GPS Source"] in "GPSD":
                            print("[+] Stopping GPSD")
                            break
                            # If GPSD is connected retry every 5 seconds
                except:
                    time.sleep(5)
                    print("[!] Error in GPS thread")
                    #     pass
            time.sleep(0.5)
        self._stop.set()

    def stopped(self):
        return self._stop.isSet()

    def stop(self):
        self._stop.set()

    def getLastGoodGPSFix(self):
        """
        Gets the last co-ords that the thread was updated with.
        :return:  A dictory in the following format
                gpsData = {'Latitude': "", 'Longitude': "", 'fix': "", 'connected': "", 'Altitude': "", 'speed': "", 'heading': ""}
        """
        return self.environData

    def updateVirtualGPS(self, gps_params):
        """
        Stops the GPS_Thread from updating it co-ords using GPSD.
        @see useGPSD
        :param gps_params: A dictionary containing new co-ords to use or can be None to use last co-ords

        """

        # if not self.VIRTUAL_MODE:
        #     print("[+] GPS: Virtual mode")
        self.environData["GPS Source"] = "Static"

        if gps_params is not None:
            try:
                self.environData['fix'] = int(gps_params['fix'])
            except KeyError:
                pass
            except ValueError:
                pass

            try:
                self.environData['Latitude'] = float(gps_params['Latitude'])
            except KeyError:
                pass
            except ValueError:
                pass

            try:
                self.environData['Longitude'] = float(gps_params['Longitude'])
            except KeyError:
                pass
            except ValueError:
                pass

            try:
                self.environData['connected'] = gps_params['connected']
            except KeyError:
                pass
            except ValueError:
                pass

            try:
                self.environData['Altitude'] = float(gps_params['Altitude'])
            except KeyError:
                pass
            except ValueError:
                pass

            try:
                self.environData['speed'] = float(gps_params['speed'])
            except KeyError:
                pass
            except ValueError:
                pass

            try:
                self.environData['heading'] = float(gps_params['heading'])
            except KeyError:
                pass
            except ValueError:
                pass

            try:
                self.environData['last_update_time'] = gps_params['last_update_time']
            except KeyError:
                pass
            except ValueError:
                pass

            try:
                self.environData['hdop'] = float(gps_params['hdop'])
            except KeyError:
                pass
            except ValueError:
                pass

            try:
                self.environData['vdop'] = gps_params['vdop']
            except KeyError:
                pass
            except ValueError:
                pass
                # print self.environData

    def useGPSD(self):
        """
        Starts the GPS thread using GPSD again.
        """
        self.environData["GPS Source"] = "GPSD"

    def getGpsMode(self):
        return self.environData["GPS Source"]


if "__main__" == __name__:
    gps = GPSThread()
    gps.useGPSD()
    gps.start()

    gps_data = gps.getLastGoodGPSFix()
    print(gps_data['Latitude'])
    print(gps_data['Longitude'])
