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
     * Updates patrol groups for all settlements in a world
     */
    public static void updateSettlementPatrols(Level level) {
        Map<BlockPos, SettlementData> worldSettlements = settlements.get(level);
        if (worldSettlements != null) {
            // Обновляем все существующие поселения
            for (SettlementData data : worldSettlements.values()) {
                data.updatePatrolGroups();
            }
        } else {
            // Если карта поселений ещё не инициализирована, создаем её
            settlements.put(level, new HashMap<>());
            
            // Находим все ратуши в мире
            List<BlockPos> townHalls = SettlementManager.getSettlementBlocksOfType(level, 
                    shadowshiftstudio.animalinvaders.block.custom.BobrittoTownHallBlock.class);
            
            // Для каждой ратуши создаем поселение и обновляем патрульные группы
            for (BlockPos townHall : townHalls) {
                SettlementData data = getOrCreateSettlement(level, townHall);
                
                // Находим всех Бобритто вокруг ратуши в радиусе 50 блоков
                level.getEntitiesOfClass(BobrittoBanditoEntity.class, 
                        new net.minecraft.world.phys.AABB(
                                townHall.getX() - 50, townHall.getY() - 20, townHall.getZ() - 50,
                                townHall.getX() + 50, townHall.getY() + 20, townHall.getZ() + 50))
                        .forEach(bobrito -> {
                            // Устанавливаем центр поселения для Бобритто и регистрируем в поселении
                            bobrito.setSettlementCenter(townHall);
                            data.registerBobrito(bobrito);
                        });
                
                // Обновляем патрульные группы
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
                
                // Усиливаем характеристики лидера и генерируем маршрут патрулирования
                leader.boostLeaderStats();
                leader.generatePatrolPath();
                
                System.out.println("Created patrol group with leader: " + leader.getUUID() + 
                                  " Health: " + leader.getHealth() + "/" + leader.getMaxHealth());
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
                // Находим лучшего кандидата (с наибольшим здоровьем)
                followers.sort((b1, b2) -> Float.compare(b2.getHealth(), b1.getHealth()));
                
                // Выбираем первого подходящего последователя для продвижения до лидера
                BobrittoBanditoEntity followerBobrito = followers.remove(0);
                
                if (followerBobrito != null && followerBobrito.isAlive()) {
                    System.out.println("Promoting new leader: " + followerBobrito.getUUID());
                    
                    // Трансформируем обычного Бобритто в особую сущность лидера
                    BobrittoBanditoEntity newLeader = shadowshiftstudio.animalinvaders.entity.custom.bobrittobandito.BobrittoBanditoLeaderEntity
                            .transformFromRegular(followerBobrito);
                    
                    if (newLeader == null) {
                        // Если трансформация не удалась, используем обычного Бобритто
                        newLeader = followerBobrito;
                        newLeader.setPatrolLeader(true);
                        newLeader.setPatrolFollower(false);
                        newLeader.boostLeaderStats();
                    }
                    
                    // Устанавливаем нового лидера
                    leader = newLeader;
                    
                    // Обновляем всех последователей, чтобы они следовали за новым лидером
                    for (BobrittoBanditoEntity follower : followers) {
                        follower.setLeaderUUID(leader.getUUID());
                    }
                    
                    // Генерируем новые точки патрулирования для нового лидера
                    leader.generatePatrolPath();
                    leader.setPatrolState(patrolState);
                    
                    System.out.println("New leader stats: Health=" + leader.getHealth() + "/" + leader.getMaxHealth());
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
                    .collect(java.util.stream.Collectors.toList()); // Используем изменяемый список
            
            if (availableBobritos.size() >= PATROL_SIZE) {
                // Выбираем обычного Бобритто для превращения в лидера
                BobrittoBanditoEntity regularBobrito = availableBobritos.remove(0);
                
                // Трансформируем обычного Бобритто в особую сущность лидера
                BobrittoBanditoEntity leader = shadowshiftstudio.animalinvaders.entity.custom.bobrittobandito.BobrittoBanditoLeaderEntity
                        .transformFromRegular(regularBobrito);
                
                if (leader == null) {
                    // Если трансформация не удалась, используем обычного Бобритто
                    leader = regularBobrito;
                    leader.setPatrolLeader(true);
                    leader.boostLeaderStats();
                }
                
                PatrolGroup newGroup = new PatrolGroup(leader);
                
                // Логируем создание группы и лидера
                System.out.println("Creating new patrol group with leader: " + leader.getUUID());
                
                // Add followers - выбираем ближайших к лидеру, чтобы они были физически рядом
                BobrittoBanditoEntity finalLeader = leader;
                availableBobritos.sort((b1, b2) -> {
                    double d1 = b1.distanceToSqr(finalLeader);
                    double d2 = b2.distanceToSqr(finalLeader);
                    return Double.compare(d1, d2);
                });
                
                // Добавляем ближайших как последователей
                for (int i = 0; i < PATROL_SIZE - 1 && i < availableBobritos.size(); i++) {
                    BobrittoBanditoEntity follower = availableBobritos.get(i);
                    newGroup.addFollower(follower);
                    System.out.println("Added follower: " + follower.getUUID() + " to leader: " + leader.getUUID());
                    
                    // Телепортируем последователей ближе к лидеру (если они слишком далеко)
                    if (follower.distanceToSqr(leader) > 100) { // Если дальше 10 блоков
                        // Вычисляем позицию рядом с лидером
                        double angle = 2 * Math.PI * i / (PATROL_SIZE - 1);
                        double offsetX = Math.cos(angle) * 2.0; // 2 блока от лидера
                        double offsetZ = Math.sin(angle) * 2.0;
                        
                        follower.teleportTo(
                            leader.getX() + offsetX,
                            leader.getY(),
                            leader.getZ() + offsetZ
                        );
                        System.out.println("Teleported follower closer to leader");
                    }
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