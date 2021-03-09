# HomeKit Accessory Protocol (HAP)
Readme coming after the implementation is finished.

## Required dependencies:
- GSON

This project implements HomeKit Accessory Protocol based on HAP Specification provided by Apple. 

### This project uses:
- [x] SRP (secure remote password) for early pairing setup.
- [x] mDNS service discovery for consistent service advertisement.
- [x] Ed25519 and Curve25519 for consistent and encrypted communication between controllers and the bridge.

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
