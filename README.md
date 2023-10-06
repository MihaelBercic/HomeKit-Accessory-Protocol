# HomeKit Accessory Protocol (HAP)
HomeKit Accessory Protocol based on HAP Specification provided by Apple.

## Why?
This project was developed with interest in privacy and understanding of how the HomeKit Accessory Protocol works to provide safe and secure communication between our iDevices and the server itself.

## This project implements
- [x] SRP (secure remote password) for early pairing setup.
- [x] mDNS service discovery for consistent service advertisement.
- [x] Ed25519 and Curve25519 for consistent and encrypted communication between controllers and the bridge.

## Get started
```bash
git clone https://github.com/MihaelBercic/HomeKit-Accessory-Protocol.git
cd HomeKit-Accessory-Protocol       # Move to the cloned folder.

./gradlew jar                       # Run the jar building task.

nano config.json                    # edit config.json and then.
java -jar build/libs/HAP-1.0.jar    # run the jar built.
```

## Required dependencies
- [GSON](https://github.com/google/gson)

---
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
