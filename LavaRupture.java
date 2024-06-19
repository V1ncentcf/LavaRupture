package org.example;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.*;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;
import org.checkerframework.checker.units.qual.A;

import java.util.*;

import static org.bukkit.Material.LAVA;
import static org.bukkit.Material.MAGMA_BLOCK;

public class LavaRupture extends LavaAbility implements AddonAbility, ComboAbility {
    @Attribute(Attribute.COOLDOWN)
    private long cooldown;
    @Attribute(Attribute.RANGE)
    private double range;
    @Attribute(Attribute.SPEED)
    private double speed;
    @Attribute(Attribute.SELECT_RANGE)
    private double sourceRange;
    @Attribute("Spike" + Attribute.DAMAGE)
    private double spikeDamage;
    @Attribute("Line" + Attribute.DAMAGE)
    private double damage;
    @Attribute(Attribute.KNOCKUP)
    private double knockUp;
    @Attribute(Attribute.KNOCKBACK)
    private double knockback;
    @Attribute(Attribute.RADIUS)
    private double radius;
    @Attribute(Attribute.HEIGHT)
    private double height;
    @Attribute("Spike" + Attribute.RADIUS)
    private double spikeHitbox;
    @Attribute("Line" + Attribute.RADIUS)
    private double lineHitbox;
    private long lineRevertTime;
    private long lavaRevertTime;
    private int startDelay;
    private boolean controllable;
    private Block nextBlock;
    private Block nextBlockAbove;
    private Block nextBlockBelow;
    private Location origin;
    private long revertTime;
    private Location location;
    private Location newLocation;
    private Vector direction;
    private int climbHeight;
    private int spikeDelay;
    private long time;
    private int spikeTimer;
    private double spikeLayerHeight;
    private boolean hasStarted = false;
    private Set<Entity> hurt;
    private boolean hasSpiked;
    public static final String METADATA_KEY_MAGMABLOCK = "Vincentcf:/LavaRupture://Magma";
    public static final String METADATA_KEY_LAVAWAVE = "Vincentcf:/LavaRupture://LavaWave";
    private static final FixedMetadataValue METADATA_VALUE = new FixedMetadataValue(ProjectKorra.plugin,1);
    private static final BlockFace[] HORIZONTAL_CIRCLE_FACES = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
    private static final int[][] CORNERS = {{0, 0}, {1, 1}, {-1, 0}, {1, -1}};

    public LavaRupture(Player player) {
        super(player);

        if (!bPlayer.canBendIgnoreBinds(this)) return;

        cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Vincentcf.LavaRupture.Cooldown");
        range = ConfigManager.getConfig().getDouble("ExtraAbilities.Vincentcf.LavaRupture.Range");
        speed = ConfigManager.getConfig().getDouble("ExtraAbilities.Vincentcf.LavaRupture.Speed");
        damage = ConfigManager.getConfig().getDouble("ExtraAbilities.Vincentcf.LavaRupture.Damage");
        sourceRange = ConfigManager.getConfig().getDouble("ExtraAbilities.Vincentcf.LavaRupture.SourceRange");
        lineHitbox = ConfigManager.getConfig().getDouble("ExtraAbilities.Vincentcf.LavaRupture.lineHitbox");
        climbHeight = ConfigManager.getConfig().getInt("ExtraAbilities.Vincentcf.LavaRupture.ClimbHeight");
        controllable = ConfigManager.getConfig().getBoolean("ExtraAbilities.Vincentcf.LavaRupture.Controllable");
        lineRevertTime = ConfigManager.getConfig().getLong("ExtraAbilities.Vincentcf.LavaRupture.LineRevertTime");
        lavaRevertTime = ConfigManager.getConfig().getLong("ExtraAbilities.Vincentcf.LavaRupture.LavaRevertTime");

        radius = ConfigManager.getConfig().getDouble("ExtraAbilities.Vincentcf.LavaRupture.Spike.Radius");
        height = ConfigManager.getConfig().getDouble("ExtraAbilities.Vincentcf.LavaRupture.Spike.Height");
        spikeDamage = ConfigManager.getConfig().getDouble("ExtraAbilities.Vincentcf.LavaRupture.Spike.spikeDamage");
        spikeHitbox = ConfigManager.getConfig().getDouble("ExtraAbilities.Vincentcf.LavaRupture.Spike.spikeHitbox");
        startDelay = ConfigManager.getConfig().getInt("ExtraAbilities.Vincentcf.LavaRupture.Spike.startDelay");
        spikeDelay = ConfigManager.getConfig().getInt("ExtraAbilities.Vincentcf.LavaRupture.Spike.spikeDelay");
        revertTime = ConfigManager.getConfig().getLong("ExtraAbilities.Vincentcf.LavaRupture.Spike.revertTime");
        knockUp = ConfigManager.getConfig().getDouble("ExtraAbilities.Vincentcf.LavaRupture.Spike.KnockUp");

        location = getTargetLocation();
        origin = location.getBlock().getLocation().add(0.5, 0.5, 0.5);
        double originX = origin.getX();
        double originZ = origin.getZ();
        double endX = getTargetLocation().getX();
        double endZ = getTargetLocation().getZ();
        direction = new Vector(endX - originX, 0.0D, endZ - originZ).normalize();
        hasStarted = false;
        this.time = System.currentTimeMillis();
        spikeLayerHeight = 0;
        hurt = new HashSet<>();
        spikeTimer = startDelay;

        start();
    }

    @Override
    public void progress() {

        if (!hasStarted && getTargetLocation().getBlock().getType() != Material.LAVA && getTargetLocation().getBlock().getType() != Material.MAGMA_BLOCK) {
            player.sendMessage("stop1");
            remove();
            return;
        }
        hasStarted = true;

        if (hasSpiked) {

            player.sendMessage("test2");
            Location curLocation = location.clone().getBlock().getLocation().add(0.5, 0.5, 0.5);

            // Magma base on the ground
            forBlockCircle(curLocation, radius, (block) -> {
                if (isEarthbendable(block) || block.getType() == Material.MAGMA_BLOCK && block.hasMetadata(LavaRupture.METADATA_KEY_MAGMABLOCK) && !RegionProtection.isRegionProtected(this, block.getLocation())) {
                    TempBlock spikeBaseTB = new TempBlock(block, MAGMA_BLOCK);
                    Block metaDataBlock = spikeBaseTB.getBlock();
                    metaDataBlock.setMetadata(METADATA_KEY_MAGMABLOCK,METADATA_VALUE);
                    long rTime = (((int) height - 4) * spikeDelay * 100) + revertTime;
                    spikeBaseTB.setRevertTime(rTime);
                    spikeBaseTB.setRevertTask(() -> metaDataBlock.removeMetadata(METADATA_KEY_MAGMABLOCK, ProjectKorra.plugin));

                    List<Entity> targets = GeneralMethods.getEntitiesAroundPoint(block.getLocation(), spikeHitbox);
                    for (Entity e : targets) {
                        if (!(e instanceof Player)) continue;
                        Vector knockUpPush = new Vector(0, knockUp, 0);
                        e.setVelocity(knockUpPush);
                        if (!hurt.contains(e)) {
                            if (e.getUniqueId() != player.getUniqueId()) {
                                DamageHandler.damageEntity(e, damage, this);
                                hurt.add(e);
                            }
                        }
                    }
                }
            });


            // Creating spike
            spikeTimer -= 1;

            if (spikeTimer <= 0) {
                curLocation.add(0, (spikeLayerHeight + 1), 0);
                double r = (radius * (1 - (spikeLayerHeight / (height + 1))));
                long rTime = (((int) height - (int) spikeLayerHeight) * spikeDelay * 100) + revertTime;

                forBlockCircle(curLocation, r, (block) -> {

                    List<Entity> targets = GeneralMethods.getEntitiesAroundPoint(block.getLocation(), spikeHitbox);
                    for (Entity e : targets) {
                        if (!(e instanceof Player)) continue;
                        Vector knockUpPush = new Vector(0, knockUp, 0);
                        e.setVelocity(knockUpPush);
                        if (!hurt.contains(e)) {
                            if (e.getUniqueId() != player.getUniqueId()) {
                                DamageHandler.damageEntity(e, damage, this);
                                hurt.add(e);
                            }
                        }
                    }

                    Block blockBelow = block.getLocation().add(0, -1, 0).getBlock();
                    if (isEarthbendable(blockBelow) || blockBelow.getType() == MAGMA_BLOCK) {
                        TempBlock tb = new TempBlock(block, MAGMA_BLOCK);
                        Block metaDataBlock = tb.getBlock();
                        metaDataBlock.setMetadata(METADATA_KEY_MAGMABLOCK, METADATA_VALUE);
                        tb.setRevertTime(rTime);
                        tb.setRevertTask(() -> metaDataBlock.removeMetadata(METADATA_KEY_MAGMABLOCK, ProjectKorra.plugin));
                    }
                });
                spikeTimer = spikeDelay;
                spikeLayerHeight += 1;
            }

            if (spikeLayerHeight == height) {
                remove();
                return;
            }
            return;
        }

        if (location.distanceSquared(origin) > range*range) {
            player.sendMessage("stop2");
            remove();
            return;
        }

        if (controllable && player.isSneaking()) {
            double playerX = player.getEyeLocation().getX();
            double playerZ = player.getEyeLocation().getZ();
            double endX = getTargetLocation().getX();
            double endZ = getTargetLocation().getZ();
            direction = new Vector(endX - playerX, 0.0D, endZ - playerZ).normalize();
        }

        // When target is hit
        List<Entity> targets = GeneralMethods.getEntitiesAroundPoint(location.getBlock().getLocation().add(0.5, 0.5, 0.5), spikeHitbox);
        if (!targets.isEmpty()) {
            for (Entity e : targets) {
                if (!(e instanceof Player)) continue;
                if (e.getUniqueId() != player.getUniqueId()) {
                    player.sendMessage("targets: " + targets);
                    DamageHandler.damageEntity(e, damage, this);
                    hasSpiked = true;
                }
            }
        }

        TempBlock tb = new TempBlock(location.getBlock(), MAGMA_BLOCK);
        tb.setRevertTime(lineRevertTime);
        Block metaDataBlock = tb.getBlock();
        metaDataBlock.setMetadata(METADATA_KEY_MAGMABLOCK, METADATA_VALUE);
        tb.setRevertTask(() -> metaDataBlock.removeMetadata(METADATA_KEY_MAGMABLOCK, ProjectKorra.plugin));

/*        TempBlock lineTB = new TempBlock(location.add(0, 1, 0).getBlock(), LAVA, GeneralMethods.getLavaData(5));
        lineTB.setRevertTime(lavaRevertTime);
        Block metaDataLavaBlock = lineTB.getBlock();
        metaDataLavaBlock.setMetadata(METADATA_KEY_LAVAWAVE, METADATA_VALUE);
        lineTB.setRevertTask(() -> metaDataLavaBlock.removeMetadata(METADATA_KEY_LAVAWAVE, ProjectKorra.plugin));*/

        Block locationBlock = location.getBlock();

        if (!isTransparent(nextBlockAbove)) {

        }

        if (!isTransparent(nextBlockAbove)) {
            location = nextBlockAbove.getLocation();
            direction = new Vector(0, 1, 0);
        }

        location.add(direction);

    } // Progress ends


    private Location getTargetLocation() {

        Location location = player.getEyeLocation();
        Vector newDir = location.getDirection().multiply(0.9);

        for (double i = 0; i <= sourceRange; i+=0.9) {

            if (location.getBlock().getType() == Material.LAVA || location.getBlock().getType() == Material.MAGMA_BLOCK) {
                return location;
            }
            location.add(newDir);
        }
        return location;
    }

    void hasSpiked() {
        hasSpiked = true;
    }

    public static void forBlockCircle(Location center, double radius, Consumer<Block> usage) {
        Set<Block> checked = new HashSet<>();
        Queue<Block> searchQueue = new LinkedList<>();
        searchQueue.add(center.getBlock());
        checked.add(center.getBlock());

        boolean empty = false;
        Location cornerCheck = center.clone();

        while (!empty) {
            Block current = searchQueue.poll();

            for (BlockFace face : HORIZONTAL_CIRCLE_FACES) {
                Block block = current.getRelative(face);
                if (checked.contains(block)) {
                    continue;
                }

                block.getLocation(cornerCheck);

                for (int[] xz : CORNERS) {
                    cornerCheck.add(xz[0], 0, xz[1]);
                    if (cornerCheck.distanceSquared(center) <= radius * radius) {
                        searchQueue.add(block);
                        checked.add(block);
                        break;
                    }
                }
            }

            usage.accept(current);
            empty = searchQueue.isEmpty();
        }
    }


    @Override
    public boolean isSneakAbility() {
        return true;
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    @Override
    public long getCooldown() {
        return cooldown;
    }

    @Override
    public String getName() {
        return "LavaRupture";
    }

    @Override
    public String getDescription() {
        return "Unleash a blazing line of magma along the ground that burns everything in its path. Upon reaching its end, the magma erupts into a towering spike, hardening into a sharp, fiery hazard ready to deal damage to anything nearby.";
    }

    @Override
    public String getInstructions() {
        return "VolcanicFlow (Left Click 2x) -> Shockwave (Hold Shift) -> Shockwave (Left Click while looking at lava/magma)";
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public void load() {

        ConfigManager.getConfig().addDefault("ExtraAbilities.Vincentcf.LavaRupture.Cooldown", 8000);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Vincentcf.LavaRupture.Range", 25);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Vincentcf.LavaRupture.Speed", 10);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Vincentcf.LavaRupture.Damage", 1);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Vincentcf.LavaRupture.SourceRange", 10);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Vincentcf.LavaRupture.lineHitbox", 1);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Vincentcf.LavaRupture.ClimbHeight", 3);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Vincentcf.LavaRupture.Controllable", true);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Vincentcf.LavaRupture.LineRevertTime", 500);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Vincentcf.LavaRupture.LavaRevertTime", 100);

        ConfigManager.getConfig().addDefault("ExtraAbilities.Vincentcf.LavaRupture.Spike.Radius", 4);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Vincentcf.LavaRupture.Spike.Height", 10);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Vincentcf.LavaRupture.Spike.spikeDamage", 2);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Vincentcf.LavaRupture.Spike.spikeHitbox", 1);
        ConfigManager.getConfig().addDefault(("ExtraAbilities.Vincentcf.LavaRupture.Spike.spikeDelay"), 2);
        ConfigManager.getConfig().addDefault(("ExtraAbilities.Vincentcf.LavaRupture.Spike.startDelay"), 4);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Vincentcf.LavaRupture.Spike.revertTime", 3000);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Vincentcf.LavaRupture.Spike.KnockUp", 1.5);

        Listener listener = new LavaRuptureListener();
        ProjectKorra.plugin.getServer().getPluginManager().registerEvents(listener, ProjectKorra.plugin);
        ConfigManager.defaultConfig.save();

        ProjectKorra.plugin.getLogger().info("Successfully enabled " + getName() + " " + getVersion() + " by " + getAuthor());
    }

    @Override
    public void stop() {
        remove();
    }

    @Override
    public String getAuthor() {
        return "Vincentcf";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public Object createNewComboInstance(Player player) {
        return new LavaRupture(player);
    }

    @Override
    public ArrayList<ComboManager.AbilityInformation> getCombination() {
        ArrayList<ComboManager.AbilityInformation> combo = new ArrayList<>();
        combo.add(new AbilityInformation("VolcanicFlow", ClickType.LEFT_CLICK));
        combo.add(new AbilityInformation("VolcanicFlow", ClickType.LEFT_CLICK));
        combo.add(new AbilityInformation("Shockwave", ClickType.SHIFT_DOWN));
        combo.add(new AbilityInformation("Shockwave", ClickType.LEFT_CLICK));
        return combo;
    }
}