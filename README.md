# HomeKit Accessory Protocol (HAP)
HomeKit Accessory Protocol based on HAP Specification provided by Apple. 

## Required dependencies
- [GSON](https://github.com/google/gson)

---
Preview of a working bridge with 4 accessories:

<img src="https://user-images.githubusercontent.com/13570480/110522330-10ba3d00-8111-11eb-8e0a-4853919f6d11.png" width="200">

## This project implements
- [x] SRP (secure remote password) for early pairing setup.
- [x] mDNS service discovery for consistent service advertisement.
- [x] Ed25519 and Curve25519 for consistent and encrypted communication between controllers and the bridge.

## Example Configuration file (config.json)
```json
{
    "accessories": [
        {
            "mac": "FF:AA:BB:CC:DD:EE",
            "name": "Balcony Door",
            "type": "ShellySwitch",
            "ip": "192.168.1.139"
        },
        {
            "mac": "FF:CC:AA:CC:DD:EE",
            "name": "Balcony Window",
            "type": "ShellySwitch",
            "ip": "192.168.1.113"
        }
    ]
}    
```
