pragma solidity ^0.8.22;

import "Lottery.sol";

contract Taxpayer {
    uint age; 
    bool isMarried; 
    bool iscontract;

    /* Reference to spouse if person is married, address(0) otherwise */
    address public spouse; 

    address parent1; 
    address parent2; 

    /* Constant default income tax allowance */
    uint constant DEFAULT_ALLOWANCE = 5000;

    /* Constant income tax allowance for Older Taxpayers over 65 */
    uint constant ALLOWANCE_OAP = 7000;

    /* Income tax allowance */
    uint tax_allowance; 
    uint income; 
    uint256 rev;

    // Track if we've received lottery bonus.
    bool public receivedLotteryBonus;

    //Parents are taxpayers
    constructor(address p1, address p2) {
        age = 0;
        isMarried = false;
        parent1 = p1;
        parent2 = p2;
        spouse = address(0);
        income = 0;
        tax_allowance = DEFAULT_ALLOWANCE;
        iscontract = true;
        receivedLotteryBonus = false;
    } 

    // Return the age of a person.
    function getAge() public view returns (uint) {
        return age;
    }

    // Check if an address is a valid Taxpayer contract.
    function isValidTaxpayer(address addr) internal view returns (bool) {
        if (addr == address(0)) return false;

        uint256 size;
        assembly { size := extcodesize(addr) }
        if (size == 0) return false;

        try Taxpayer(addr).isContract() returns (bool result) {
            return result;
        } catch {
            return false;
        }
    }

    //We require new_spouse != address(0);
    function marry(address new_spouse) public {
        // Cannot merry address(0).
        require(new_spouse != address(0), "Invalid spouse");
        // Cannot marry if already married.
        require(spouse == address(0), "Already married");
        // Cannot marry self.
        require(new_spouse != address(this), "Cannot marry self");
        require(isValidTaxpayer(new_spouse), "Spouse must be valid Taxpayer");

        Taxpayer other = Taxpayer(new_spouse);
        // Check that spouse is not already married to someone else.
        require(other.spouse() == address(0) || other.spouse() == address(this),                "Spouse already married to someone else");

        spouse = new_spouse;
        isMarried = true;

        // Update spouse's state.
        if (other.spouse() != address(this)) {
            other.marry(address(this));
        }
    }

    function divorce() public {
        require(isMarried, "Not married");
        require(spouse != address(0), "Invalid spouse state");

        address ex_spouse = spouse;

        spouse = address(0);
        isMarried = false;

        // Update ex_spouse's state. 
        if (isValidTaxpayer(ex_spouse)) {
            Taxpayer other = Taxpayer(ex_spouse);
            if (other.spouse() == address(this)) {
                other.divorce();
            }
        }
    }

    /* Receive allowance form spource. */
    function receiveAllowance(uint change) public {
        // Only my spouse can send me allowance
        require(msg.sender == spouse, "Only spouse can transfer allowance");
        require(spouse != address(0), "Must have valid spouse");
        tax_allowance = tax_allowance + change;
    }

    /* Transfer part of tax allowance to own spouse */
    function transferAllowance(uint change) public {
        require(isMarried, "Must be married to transfer allowance");
        require(spouse != address(0), "Invalid spouse address");
        require(tax_allowance >= change, "Insufficient allowance");

        tax_allowance = tax_allowance - change;
        Taxpayer(spouse).receiveAllowance(change);
    }

    function haveBirthday() public {
        age++;
        // Check if they just turned 65.
        if (age == 65 && !receivedLotteryBonus) {
            // We use addition to preserve any previous transfers between spouses.
            tax_allowance = tax_allowance + (ALLOWANCE_OAP - DEFAULT_ALLOWANCE);
        }
    }

    function setTaxAllowance(uint ta) public {
        // Prevents other Taxpayers arbitrarily change our allowance.
        require(Lottery(msg.sender).isContract(), 
                "Only Lottery can set absolute allowance");

            if (ta == ALLOWANCE_OAP) {
                receivedLotteryBonus = true;
            }

            tax_allowance = ta;
    }

    function getTaxAllowance() public view returns(uint) {
        return tax_allowance;
    }

    function isContract() public view returns(bool){
        return iscontract;
    }

    function joinLottery(address lot, uint256 r) public {
        Lottery l = Lottery(lot);
        l.commit(keccak256(abi.encode(r)));
        rev = r;
    }

    function revealLottery(address lot, uint256 r) public {
        Lottery l = Lottery(lot);
        l.reveal(r);
        rev = 0;
    }

    /* ============================= Invariants ============================= */
    // Part 1

    // If I am married to x, x must be married to me.
    function echidna_simmetric_marriage() public view returns (bool) {
        if (spouse != address(0)) {
            if (!isValidTaxpayer(spouse)) return true;
            return Taxpayer(spouse).spouse() == address(this);
        }
        return true;    
    }

    // If I have a spouse, I must be marked as married.
    function echidna_marriage_status() public view returns (bool) {
        if (spouse != address(0)) {
            return isMarried == true;
        } else {
            return isMarried == false;
        }
    }

    // A person cannot be married to themselves.
    function echidna_no_self_marriage() public view returns (bool) {
        return spouse != address(this);
    }

    // A person cannot be defined as their own parent.
    function echidna_no_self_parent() public view returns (bool) {
        return parent1 != address(this) && parent2 != address(this);
    }

    // Parents must be distinct.
    function echidna_parents_distinct() public view returns (bool) {
        if (parent1 == address(0) || parent2 == address(0)) return true;
        return parent1 != parent2;
    }

    // Spouse must be valid Taxpayer.
    function echidna_spouse_is_valid_taxpayer() public view returns (bool) {
        if (spouse == address(0)) return true;
        return isValidTaxpayer(spouse);
    }

    // Part 2

    // Single taxpayers must have at least their age-based minimum.
    // Married couples must have combined allowance equal to sum of their minimums.    
    function echidna_tax_allowance() public view returns (bool) {
        if (spouse != address(0)) {
            uint256 myAllowance = tax_allowance;
            uint256 spouseAllowance = Taxpayer(spouse).getTaxAllowance();
            
            return (myAllowance + spouseAllowance) == (2 * DEFAULT_ALLOWANCE);
        } else {
            return tax_allowance >= DEFAULT_ALLOWANCE;
        }
    }

    // Part 3

    // Allowance must never drop below age-based minimum.
    // Married couple must have total allowance preserved.
    function echidna_total_allowance_preserved_age() public view returns (bool) {
        uint myMin = age >= 65 ? ALLOWANCE_OAP : DEFAULT_ALLOWANCE;

        if (isMarried && spouse != address(0) && isValidTaxpayer(spouse)) {
            Taxpayer sp = Taxpayer(spouse);
            uint spMin = sp.getAge() >= 65 ? ALLOWANCE_OAP : DEFAULT_ALLOWANCE;

            // The min sum must be preserved.
            uint totalMin = myMin + spMin;
            uint total = tax_allowance + sp.getTaxAllowance();
            return total == totalMin;
        } else {
            return tax_allowance >= myMin;
        }
    }

    // Allowance is never zero. 
    function echidna_allowance_never_zero() public view returns (bool) {
        return tax_allowance > 0;
    }

    // If someone received lottery bonus (7000) and is under 65, they should have that amount
    // unless they transferred some to spouse.
    function echidna_lottery_bonus_consistency() public view returns (bool) {
        if (receivedLotteryBonus && age < 65 && !isMarried) {
            // Single person under 65 with lottery bonus should have ALLOWANCE_OAP.
            return tax_allowance == ALLOWANCE_OAP;
        }
        return true;
    }

    // Anyone 65 or older should have at least ALLOWANCE_OAP unless married and transferred to spouse.
    function echidna_senior_minimum_allowance() public view returns (bool) {
        if (age >= 65 && !isMarried) {
            return tax_allowance >= ALLOWANCE_OAP;
        }
        return true;
    }

}

contract TestTaxpayer is Taxpayer {
    constructor() Taxpayer(address(0), address(0)) { }
}
