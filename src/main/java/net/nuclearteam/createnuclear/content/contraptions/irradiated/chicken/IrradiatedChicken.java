package net.nuclearteam.createnuclear.content.contraptions.irradiated.chicken;

import net.minecraft.world.entity.EntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.chicken.ChickenSoundVariant;
import net.minecraft.world.entity.animal.chicken.ChickenSoundVariants;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.nuclearteam.createnuclear.CNEntityType;
import net.nuclearteam.createnuclear.CNTags;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.IrradiatedAnimal;

import org.jetbrains.annotations.Nullable;
@SuppressWarnings("unused")
public class IrradiatedChicken extends Animal implements IrradiatedAnimal {
    private static final EntityDimensions BABY_DIMENSIONS;
    private static final EntityDataAccessor<Boolean> DATA_CONVERTING_ID = SynchedEntityData.defineId(IrradiatedChicken.class, EntityDataSerializers.BOOLEAN);
    public float flap;
    public float flapSpeed;
    public float oFlapSpeed;
    public float oFlap;
    public float flapping = 1.0F;
    private float nextFlap = 1.0F;
    public int eggTime;
    public boolean isChickenJockey;
    private int conversionTime;

    public IrradiatedChicken(EntityType<? extends IrradiatedChicken> entityType, Level level) {
        super(entityType, level);
        this.eggTime = this.random.nextInt(6000) + 6000;
        this.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CONVERTING_ID, false);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(2, new BreedGoal(this, (double)1.0F));
        this.goalSelector.addGoal(3, new TemptGoal(this, (double)1.0F, (p_335679_) -> p_335679_.is(CNTags.CNItemTags.FUEL.tag), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, (double)1.0F));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    public EntityDimensions getDefaultDimensions(Pose pose) {
        return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions(pose);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, (double)4.0F)
                .add(Attributes.MOVEMENT_SPEED, (double)0.25F);
    }

    public void aiStep() {
        super.aiStep();
        this.oFlap = this.flap;
        this.oFlapSpeed = this.flapSpeed;
        this.flapSpeed += (this.onGround() ? -1.0F : 4.0F) * 0.3F;
        this.flapSpeed = Mth.clamp(this.flapSpeed, 0.0F, 1.0F);
        if (!this.onGround() && this.flapping < 1.0F) {
            this.flapping = 1.0F;
        }

        this.flapping *= 0.9F;
        Vec3 vec3 = this.getDeltaMovement();
        if (!this.onGround() && vec3.y < (double)0.0F) {
            this.setDeltaMovement(vec3.multiply((double)1.0F, 0.6, (double)1.0F));
        }

        this.flap += this.flapping * 2.0F;
        if (this.level() instanceof ServerLevel serverLevel && this.isAlive() && !this.isBaby() && !this.isChickenJockey() && --this.eggTime <= 0) {
            this.playSound(SoundEvents.CHICKEN_EGG, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            this.spawnAtLocation(serverLevel, Items.EGG);
            this.gameEvent(GameEvent.ENTITY_PLACE);
            this.eggTime = this.random.nextInt(6000) + 6000;
        }

    }

    protected boolean isFlapping() {
        return this.flyDist > this.nextFlap;
    }

    protected void onFlap() {
        this.nextFlap = this.flyDist + this.flapSpeed / 2.0F;
    }

    // 26.2 chicken sounds come in sound sets; the irradiated chicken uses the classic one.
    private ChickenSoundVariant.ChickenSoundSet sounds() {
        ChickenSoundVariant variant = SoundEvents.CHICKEN_SOUNDS.get(ChickenSoundVariants.SoundSet.CLASSIC);
        return this.isBaby() ? variant.babySounds() : variant.adultSounds();
    }

    protected SoundEvent getAmbientSound() {
        return sounds().ambientSound().value();
    }

    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return sounds().hurtSound().value();
    }

    protected SoundEvent getDeathSound() {
        return sounds().deathSound().value();
    }

    protected void playStepSound(BlockPos pos, BlockState block) {
        this.playSound(sounds().stepSound().value(), 0.15F, 1.0F);
    }

    @Nullable
    public IrradiatedChicken getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return CNEntityType.IRRADIATED_CHICKEN.create(level, EntitySpawnReason.BREEDING);
    }

    public boolean isFood(ItemStack stack) {
        return stack.is(CNTags.CNItemTags.FUEL.tag);
    }

    protected int getBaseExperienceReward(ServerLevel level) {
        return this.isChickenJockey() ? 10 : super.getBaseExperienceReward(level);
    }

    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.isChickenJockey = input.getBooleanOr("IsChickenJockey", false);
        input.getInt("EggLayTime").ifPresent(time -> this.eggTime = time);
        int conversionTime = input.getIntOr("ConversionTime", -1);
        if (conversionTime > -1) {
            this.startConverting(conversionTime);
        }
    }

    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("IsChickenJockey", this.isChickenJockey);
        output.putInt("EggLayTime", this.eggTime);
        output.putInt("ConversionTime", this.isConverting() ? this.conversionTime : -1);
    }

    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return this.isChickenJockey();
    }

    protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
        super.positionRider(passenger, callback);
        if (passenger instanceof LivingEntity) {
            ((LivingEntity)passenger).yBodyRot = this.yBodyRot;
        }

    }

    public boolean isChickenJockey() {
        return this.isChickenJockey;
    }

    public void setChickenJockey(boolean isChickenJockey) {
        this.isChickenJockey = isChickenJockey;
    }

    @Override
    public EntityType<? extends Animal> getNormalVariant() {
        return EntityTypes.CHICKEN;
    }

    @Override
    public void readFromVanilla(Animal animal) {
        if (animal instanceof Chicken chicken) {
            this.setChickenJockey(chicken.isChickenJockey());
        }
    }

    @Override
    public void writeToVanilla(Animal animal) {
        if (animal instanceof Chicken chicken) {
            chicken.setChickenJockey(this.isChickenJockey());
        }
    }

    @Override
    public boolean isConverting() {
        return this.entityData.get(DATA_CONVERTING_ID);
    }

    @Override
    public void setConverting() {
        this.entityData.set(DATA_CONVERTING_ID, true);
    }

    @Override
    public void setConversionTime(int conversionTime) {
        this.conversionTime = conversionTime;
    }

    @Override
    public int getConversionTime() {
        return this.conversionTime;
    }

    static {
        BABY_DIMENSIONS = CNEntityType.IRRADIATED_CHICKEN.get().getDimensions().scale(0.5F).withEyeHeight(0.2975F);
    }
}