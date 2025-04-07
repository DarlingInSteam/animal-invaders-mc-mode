package shadowshiftstudio.animalinvaders.block.settlement;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import shadowshiftstudio.animalinvaders.block.custom.BobrittoHouseBlock;
import shadowshiftstudio.animalinvaders.entity.custom.bobrittobandito.BobrittoBanditoEntity;

import java.util.*;

/**
 * Manages Bobrito Bandito entities in settlements, including population and patrol groups
 */
public class BobrittoManager {
    // Map of town halls to their settlement data
    private static final Map<Level, Map<BlockPos, SettlementData>> settlements = new HashMap<>();
    
    // Constants for patrol groups
    public static final int BOBRITOS_PER_HOUSE = 5;
    public static final int HOUSES_PER_PATROL = 3;
    public static final int PATROL_SIZE = 5; // 1 leader + 4 followers
    public static final int PATROL_INNER_TIME = 5 * 60 * 20; // 5 minutes in ticks
    
    /**
     * Initializes or gets a settlement with the given town hall position
     */
    public static SettlementData getOrCreateSettlement(Level level, BlockPos townHallPos) {
        Map<BlockPos, SettlementData> worldSettlements = settlements.computeIfAbsent(level, k -> new HashMap<>());
        return worldSettlements.computeIfAbsent(townHallPos, pos -> new SettlementData(level, pos));
    }
    
    /**
     * Removes a settlement when its town hall is destroyed
     */
    public static void removeSettlement(Level level, BlockPos townHallPos) {
        Map<BlockPos, SettlementData> worldSettlements = settlements.get(level);
        if (worldSettlements != null) {
            SettlementData data = worldSettlements.remove(townHallPos);
            if (data != null) {
                data.cleanup();
            }
        }
    }
    
    /**
     * Registers a new bobrito entity with its settlement
     */
    public static void registerBobrito(BobrittoBanditoEntity bobrito) {
        Level level = bobrito.level();
        BlockPos townHallPos = SettlementManager.findNearestTownHall(level, bobrito.blockPosition());
        
        if (townHallPos != null) {
            SettlementData data = getOrCreateSettlement(level, townHallPos);
            data.registerBobrito(bobrito);
        }
    }
    
    /**
     * Updates patrol groups based on current settlement population
     */
    public static void updatePatrolGroups(Level level, BlockPos townHallPos) {
        Map<BlockPos, SettlementData> worldSettlements = settlements.get(level);
        if (worldSettlements != null) {
            SettlementData data = worldSettlements.get(townHallPos);
            if (data != null) {
                data.updatePatrolGroups();
            }
        }
    }
    
    /**
     * Gets the current capacity of a settlement based on number of houses
     */
    public static int getSettlementCapacity(Level level, BlockPos townHallPos) {
        // Count houses in the settlement
        List<BlockPos> houses = SettlementManager.getSettlementBlocksOfType(level, 
                shadowshiftstudio.animalinvaders.block.custom.BobrittoHouseBlock.class);
        
        // Filter to only houses within this settlement's influence
        houses = houses.stream()
                .filter(housePos -> {
                    BlockPos nearestTownHall = SettlementManager.findNearestTownHall(level, housePos);
                    return nearestTownHall != null && nearestTownHall.equals(townHallPos);
                })
                .toList();
        
        return houses.size() * BOBRITOS_PER_HOUSE;
    }
    
    /**
     * Represents a patrol group with one leader and followers
     */
    public static class PatrolGroup {
        private BobrittoBanditoEntity leader;
        private final List<BobrittoBanditoEntity> followers = new ArrayList<>();
        private PatrolState patrolState = PatrolState.PATROLLING_OUTER;
        private int innerPatrolTimer = 0;
        
        public PatrolGroup(BobrittoBanditoEntity leader) {
            this.leader = leader;
            if (leader != null) {
                leader.setPatrolLeader(true);
            }
        }
        
        public void addFollower(BobrittoBanditoEntity follower) {
            if (followers.size() < PATROL_SIZE - 1) {
                followers.add(follower);
                follower.setPatrolLeader(false);
                follower.setPatrolFollower(true);
                follower.setLeaderUUID(leader.getUUID());
            }
        }
        
        public void update() {
            if (leader == null || !leader.isAlive()) {
                // Leader died, promote a follower
                promoteNewLeader();
            }
            
            // Remove dead followers
            followers.removeIf(follower -> follower == null || !follower.isAlive());
            
            // Update patrol state
            if (patrolState == PatrolState.PATROLLING_INNER) {
                innerPatrolTimer--;
                if (innerPatrolTimer <= 0) {
                    patrolState = PatrolState.PATROLLING_OUTER;
                    updatePatrolBehaviors();
                }
            } else if (leader != null && leader.hasCompletedOuterPatrol()) {
                patrolState = PatrolState.PATROLLING_INNER;
                innerPatrolTimer = PATROL_INNER_TIME;
                updatePatrolBehaviors();
            }
        }
        
        private void promoteNewLeader() {
            if (!followers.isEmpty()) {
                // Promote first follower to leader
                BobrittoBanditoEntity newLeader = followers.remove(0);
                if (newLeader != null && newLeader.isAlive()) {
                    leader = newLeader;
                    leader.setPatrolLeader(true);
                    leader.setPatrolFollower(false);
                    
                    // Update all followers to follow new leader
                    for (BobrittoBanditoEntity follower : followers) {
                        follower.setLeaderUUID(leader.getUUID());
                    }
                }
            }
        }
        
        private void updatePatrolBehaviors() {
            if (leader != null) {
                leader.setPatrolState(patrolState);
            }
            
            for (BobrittoBanditoEntity follower : followers) {
                follower.setPatrolState(patrolState);
            }
        }
        
        public boolean isFull() {
            return leader != null && followers.size() >= PATROL_SIZE - 1;
        }
        
        public List<BobrittoBanditoEntity> getAllMembers() {
            List<BobrittoBanditoEntity> members = new ArrayList<>();
            if (leader != null) {
                members.add(leader);
            }
            members.addAll(followers);
            return members;
        }
    }
    
    /**
     * Patrol state representing whether the group is patrolling outside or inside the settlement
     */
    public enum PatrolState {
        PATROLLING_OUTER,  // Patrolling around the settlement perimeter
        PATROLLING_INNER   // Patrolling inside the settlement
    }
    
    /**
     * Represents the data for a single settlement
     */
    public static class SettlementData {
        private final Level level;
        private final BlockPos townHallPos;
        private final List<BobrittoBanditoEntity> bobritos = new ArrayList<>();
        private final List<PatrolGroup> patrolGroups = new ArrayList<>();
        
        public SettlementData(Level level, BlockPos townHallPos) {
            this.level = level;
            this.townHallPos = townHallPos;
        }
        
        public void registerBobrito(BobrittoBanditoEntity bobrito) {
            if (!bobritos.contains(bobrito)) {
                bobritos.add(bobrito);
                updatePatrolGroups();
            }
        }
        
        public void updatePatrolGroups() {
            // Remove references to dead entities
            bobritos.removeIf(bobrito -> bobrito == null || !bobrito.isAlive());
            
            // Calculate how many patrol groups we should have
            int houseCount = SettlementManager.getSettlementBlocksOfType(level, BobrittoHouseBlock.class).size();
            int targetPatrolCount = Math.max(1, houseCount / HOUSES_PER_PATROL);
            
            // Update existing patrol groups
            for (PatrolGroup group : patrolGroups) {
                group.update();
            }
            
            // Remove empty patrol groups
            patrolGroups.removeIf(group -> group.getAllMembers().isEmpty());
            
            // Create new patrol groups if needed
            while (patrolGroups.size() < targetPatrolCount && bobritos.size() >= PATROL_SIZE) {
                createNewPatrolGroup();
            }
        }
        
        private void createNewPatrolGroup() {
            // First, collect all non-patrol bobritos
            List<BobrittoBanditoEntity> availableBobritos = bobritos.stream()
                    .filter(bobrito -> !bobrito.isPatrolLeader() && !bobrito.isPatrolFollower())
                    .toList();
            
            if (availableBobritos.size() >= PATROL_SIZE) {
                // Select a leader
                BobrittoBanditoEntity leader = availableBobritos.get(0);
                PatrolGroup newGroup = new PatrolGroup(leader);
                
                // Add followers
                for (int i = 1; i < PATROL_SIZE && i < availableBobritos.size(); i++) {
                    newGroup.addFollower(availableBobritos.get(i));
                }
                
                patrolGroups.add(newGroup);
            }
        }
        
        public void cleanup() {
            // Reset all patrol bobritos to normal
            for (PatrolGroup group : patrolGroups) {
                for (BobrittoBanditoEntity bobrito : group.getAllMembers()) {
                    if (bobrito != null && bobrito.isAlive()) {
                        bobrito.setPatrolLeader(false);
                        bobrito.setPatrolFollower(false);
                    }
                }
            }
            
            patrolGroups.clear();
            bobritos.clear();
        }
        
        public BlockPos getTownHallPos() {
            return townHallPos;
        }
        
        public List<BobrittoBanditoEntity> getBobritos() {
            return new ArrayList<>(bobritos);
        }
        
        public List<PatrolGroup> getPatrolGroups() {
            return new ArrayList<>(patrolGroups);
        }
    }
}