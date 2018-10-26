#!/usr/bin/env python

###################################################################
#  cpuinfo - Get information about CPU
#
#      License: BSD
#      Author:  Pearu Peterson <pearu@cens.ioc.ee>
#
####################################################################

__all__ = ['cpu']

import sys, re
import os
import subprocess
import warnings
import platform
import inspect

def getoutput(cmd, successful_status=(0,), stacklevel=1):
    try:
        p = subprocess.Popen(cmd, stdout=subprocess.PIPE)
        output, _ = p.communicate()
        status = p.returncode
    except EnvironmentError as e:
        warnings.warn(str(e), UserWarning, stacklevel=stacklevel)
        return False, ''
    if os.WIFEXITED(status) and os.WEXITSTATUS(status) in successful_status:
        return True, output
    return False, output


class CPUInfoBase(object):
    """Holds CPU information and provides methods for requiring
    the availability of various CPU features.
    """

    def _try_call(self, func):
        try:
            return func()
        except:
            pass

    def __getattr__(self, name):
        if not name.startswith('_'):
            if hasattr(self, '_' + name):
                attr = getattr(self, '_' + name)
                if inspect.ismethod(attr):
                    return lambda func=self._try_call, attr=attr: func(attr)
            else:
                return lambda: None
        raise AttributeError(name)


class LinuxCPUInfo(CPUInfoBase):
    info = None

    def __init__(self):
        if self.info is not None:
            return
        info = [{}]
        ok, output = getoutput(['uname', '-m'])
        if ok:
            info[0]['uname_m'] = output.strip()
        try:
            fo = open('/proc/cpuinfo')
        except EnvironmentError as e:
            warnings.warn(str(e), UserWarning)
        else:
            for line in fo:
                name_value = [s.strip() for s in line.split(':', 1)]
                if len(name_value) != 2:
                    continue
                name, value = name_value
                if not info or name in info[-1]:  # next processor
                    info.append({})
                info[-1][name] = value
            fo.close()
        self.__class__.info = info

    def _is_AMD(self):
        return self.info[0]['vendor_id'] == 'AuthenticAMD'

    def _is_Intel(self):
        return self.info[0]['vendor_id'] == 'GenuineIntel'


class Win32CPUInfo(CPUInfoBase):
    info = None
    pkey = r"HARDWARE\DESCRIPTION\System\CentralProcessor"
    # XXX: what does the value of
    #   HKEY_LOCAL_MACHINE\HARDWARE\DESCRIPTION\System\CentralProcessor\0
    # mean?

    def __init__(self):
        try:
            import _winreg
        except ImportError:  # Python 3
            import winreg as _winreg

        if self.info is not None:
            return
        info = []
        try:
            #XXX: Bad style to use so long `try:...except:...`. Fix it!

            prgx = re.compile(r"family\s+(?P<FML>\d+)\s+model\s+(?P<MDL>\d+)" \
                              "\s+stepping\s+(?P<STP>\d+)", re.IGNORECASE)
            chnd = _winreg.OpenKey(_winreg.HKEY_LOCAL_MACHINE, self.pkey)
            pnum = 0
            while 1:
                try:
                    proc = _winreg.EnumKey(chnd, pnum)
                except _winreg.error:
                    break
                else:
                    pnum += 1
                    info.append({"Processor": proc})
                    phnd = _winreg.OpenKey(chnd, proc)
                    pidx = 0
                    while True:
                        try:
                            name, value, vtpe = _winreg.EnumValue(phnd, pidx)
                        except _winreg.error:
                            break
                        else:
                            pidx = pidx + 1
                            info[-1][name] = value
                            if name == "Identifier":
                                srch = prgx.search(value)
                                if srch:
                                    info[-1]["Family"] = int(srch.group("FML"))
                                    info[-1]["Model"] = int(srch.group("MDL"))
                                    info[-1]["Stepping"] = int(srch.group("STP"))
        except:
            print(sys.exc_value, '(ignoring)')
        self.__class__.info = info

    def _is_AMD(self):
        return self.info[0]['VendorIdentifier'] == 'AuthenticAMD'

    def _is_Intel(self):
        return self.info[0]['VendorIdentifier'] == 'GenuineIntel'


if sys.platform.startswith('linux'):  # variations: linux2,linux-i386 (any others?)
    cpuinfo = LinuxCPUInfo
elif sys.platform.startswith('win32'):
    cpuinfo = Win32CPUInfo
elif sys.platform.startswith('cygwin'):
    cpuinfo = LinuxCPUInfo
#XXX: other OS's. Eg. use _winreg on Win32. Or os.uname on unices.
else:
    cpuinfo = CPUInfoBase

cpu = cpuinfo()
