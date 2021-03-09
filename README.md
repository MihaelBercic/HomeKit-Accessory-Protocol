# HomeKit Accessory Protocol (HAP)
Readme coming after the implementation is finished.

## Required dependencies:
- GSON

This project implements HomeKit Accessory Protocol based on HAP Specification provided by Apple. 

Preview of a working bridge with 4 accessories:
![image](https://user-images.githubusercontent.com/13570480/110522330-10ba3d00-8111-11eb-8e0a-4853919f6d11.png)


### This project implements:
- [x] SRP (secure remote password) for early pairing setup.
- [x] mDNS service discovery for consistent service advertisement.
- [x] Ed25519 and Curve25519 for consistent and encrypted communication between controllers and the bridge.

# Usage

## Example Configuration file (config.json):
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
    }
  ]
}
```
**Note:** Ids should all be > 1 (greater than one and unique) due to `id = 1` belonging to the bridge itself.
