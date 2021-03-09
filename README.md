# HomeKit Accessory Protocol (HAP)
Readme coming after the implementation is finished.

## Required libraries:
- GSON

This project implements HomeKit Accessory Protocol based on HAP Specification provided by Apple. 

### This project uses:
- [x] SRP (secure remote password) **implemented**
- [x] mDNS service discovery **implemented**
- [x] Ed25519 **implemented** 
- [x] Curve25519 **implemented**

# Usage

## Configuration file (config.json):
```json
{
  "accessories": [
    {
      "id": 2,
      "name": "Window Covering",
      "type": "ShellySwitch",
      "ip": "192.168.1.150"
    },
    {
      "id": 3,
      "name": "Desk Lamp",
      "type": "ShellyBulb",
      "ip": "192.168.1.138"
    },
    ...
  ]
}

```
