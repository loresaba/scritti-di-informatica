pragma solidity ^0.8.22;
// SPDX-License-Identifier: UNLICENSED
import "Taxpayer.sol";

contract Lottery {
    address owner;
    mapping (address => bytes32) commits;
    mapping (address => uint) reveals;
    address[] revealed;

    uint256 startTime;
    uint256 revealTime;
    uint256 endTime;
    uint256 period;
    bool iscontract;

    // Track partecipation to prevent duplicates.
    mapping(address => bool) public hasRevealed;

    // Initialize the registry with the lottery period.
    constructor(uint p) {
        period = p;
        startTime = 0;
        endTime = 0;
        iscontract = true;
    } 

    // If the lottery has not started, anyone can invoke a lottery.
    function startLottery() public {
        require (startTime == 0, "Lottery already started");
        // startTime current time. Users send their committed value
        startTime = block.timestamp;
        // revealTime time for revealing. User reveal their value
        revealTime = startTime + period;
        // endTime a winner can be computed
        endTime = revealTime + period;
    }

    // A taxpayer send his own commitment. 
    function commit(bytes32 y) public {
        require(block.timestamp >= startTime);
        commits[msg.sender] = y;
    }

    //A valid taxpayer who sent his own commitment, sends the revealing value.
    function reveal(uint256 rev) public {
        require(block.timestamp >= revealTime);
        require(keccak256(abi.encode(rev))==commits[msg.sender]);
        revealed.push(msg.sender);
        reveals[msg.sender] = uint(rev);

    }

    //Ends the lottery and compute the winner.
    function endLottery() public {
        require(block.timestamp >= endTime);
        uint total = 0;
        for (uint i = 0; i < revealed.length; i++)
        total+= reveals[revealed[i]];
        Taxpayer(revealed[total%revealed.length]).setTaxAllowance(7000);
        startTime = 0;
        revealTime=0;
        endTime = 0;
    }

    function isContract() public view returns(bool) {
        return iscontract;
    }

    // Validate that an address is a Taxpayer contract.
    function isValidTaxpayer(address addr) internal view returns(bool) {
        if (addr == address(0)) return false;

        // Check if it's a contract.
        uint256 size;
        assembly { size := extcodesize(addr) }
        if (size == 0) return false;

        try Taxpayer(addr).isContract() returns (bool result) {
            return result;
        } catch {
            return false;
        }
    }

    // Commits the hash of 'val'.
    function proxy_commit(uint256 val) public {
        bytes32 hash = keccak256(abi.encode(val));
        this.commit(hash);
    }

    // Part 4

    // The same participant cannot participate multiple times.
    // Limit checks to first 10 entries to avoid Gas Limit issues in Echidna.
    function echidna_no_duplicates_in_reveal() public view returns (bool) {
        uint limit = revealed.length;
        if (limit > 10) limit = 10;

        for (uint i = 0; i < limit; i++) {
            for (uint j = i + 1; j < limit; j++) {
                if (revealed[i] == revealed[j]) {
                    return false;
                }
            }
        }
        return true;
    }

    // If the lottery ends, the 'revealed' array must be reset.
    function echidna_state_reset_correctly() public view returns (bool) {
        if (startTime == 0) {
            if (revealed.length != 0) return false;

            uint checkLimit = revealed.length > 10 ? 10 : revealed.length;
            for (uint i = 0; i < checkLimit; i++) {
                if (hasRevealed[revealed[i]]) return false;
            }
        }
        return true;
    }

    // INVARIANT 3: Revealed participants must be valid Taxpayer contracts
    // This prevents crashes when calling setTaxAllowance on the winner
    function echidna_revealed_are_taxpayers() public view returns (bool) {
        uint limit = revealed.length;
        if (limit > 10) limit = 10;

        for (uint i = 0; i < limit; i++) {
            if (!isValidTaxpayer(revealed[i])) {
                return false;
            }
        }
        return true;
    }

    // Every revealed participant must have a valid commitment.
    function echidna_reveal_requires_commit() public view returns (bool) {
        uint limit = revealed.length;
        if (limit > 10) limit = 10;

        for (uint i = 0; i < limit; i++) {
            address participant = revealed[i];
            // Must have non-zero commitment.
            if (commits[participant] == bytes32(0)) return false;
            // Must be marked as revealed.
            if (!hasRevealed[participant]) return false;
        }
        return true;
    }

    // Lottery phases must be properly ordered.
    function echidna_valid_timeline() public view returns (bool) {
        if (startTime > 0) {
            if (revealTime <= startTime) return false;
            if (endTime <= revealTime) return false;
        }
        return true;
    }

    // If someone is in revealed array, they must have hasRevealed flag set.
    function echidna_revealed_flag_consistent() public view returns (bool) {
        uint limit = revealed.length;
        if (limit > 10) limit = 10;

        for (uint i = 0; i < limit; i++) {
            if (!hasRevealed[revealed[i]]) {
                return false;
            }
        }
        return true;
    }

}

contract TestLottery is Lottery {
    constructor() Lottery(10) {}
} 
