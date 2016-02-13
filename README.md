#Java TOTP
Java class to generate steam two-factor authentication codes (TOTP), given a shared secred. 

##Example
```Java
    public String getsteamCode() {
        SteamTOTP steam = new SteamTOTP(sharedSecret, identitySecret ,0);
        try {
            return steam.getAuthCode();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
```
#steamTOTP(sharedSecret, identitySecret, timeDiff)
The class has to be instantiated with: 
* `sharedSecred` -- Your shared secret as a `String`
* `identitySecret` -- Your identity secret as a `String` 
* `timeDiff` -- The time difference between the system clock and the Steam servers

#getAuthCode() 
Returns the 5-diget steam TOTP code, as a `String`

#getConfirmationKey(unixTime, tag)
* `unixTime` -- The Unix time for which you are generating this secret. Generally should be the current time.
* `tag` -- The tag which identifies what this request (and therefore key) will be for. "conf" to load the confirmations page, "details" to load details about a trade, "allow" to confirm a trade, "cancel" to cancel it.

Returns a string with a base64 confirmation key for use with the mobile confirmations web page.


#Thanks
Thanks to [DoctorMcKay](https://github.com/DoctorMcKay/node-steam-totp) for the node version, which this code is based upon. His implementation can be found [here](https://github.com/DoctorMcKay/node-steam-totp).
