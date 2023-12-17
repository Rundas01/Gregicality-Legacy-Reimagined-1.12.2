package gregicality.legacy.modules;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import gregtech.api.modules.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

public class ModuleManager implements IModuleManager {

    private static final ModuleManager INSTANCE = new ModuleManager();
    //private static final String MODULE_CFG_FILE_NAME = "modules.cfg";
    //private static final String MODULE_CFG_CATEGORY_NAME = "modules";
    //private static File configFolder;

    private final Map<String, IModuleContainer> containers = new HashMap<>();
    private final Map<ResourceLocation, IGregTechModule> sortedGCYLRModules = new LinkedHashMap<>();
    private final Set<IGregTechModule> loadedGCYLRModules = new LinkedHashSet<>();

    private IModuleContainer currentContainer;

    private ModuleStage currentStage = ModuleStage.C_SETUP;
    private final Logger logger = LogManager.getLogger("GregTech Module Loader");
    //private Configuration config;

    private ModuleManager() {
    }

    public static ModuleManager getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isModuleEnabled(ResourceLocation id) {
        return sortedGCYLRModules.containsKey(id);
    }

    public boolean isModuleEnabled(IGregTechModule module) {
        return true;
        //GregTechModule annotation = module.getClass().getAnnotation(GregTechModule.class);
        //String comment = getComment(module);
        //Property prop = getConfiguration().get(MODULE_CFG_CATEGORY_NAME, annotation.containerID() + ":" + annotation.moduleID(), true, comment);
        //return prop.getBoolean();
    }

    @Override
    public IModuleContainer getLoadedContainer() {
        return currentContainer;
    }

    @Override
    public ModuleStage getStage() {
        return currentStage;
    }

    @Override
    public boolean hasPassedStage(ModuleStage stage) {
        return currentStage.ordinal() > stage.ordinal();
    }

    @Override
    public void registerContainer(IModuleContainer container) {
        if (currentStage != ModuleStage.C_SETUP) {
            logger.error("Failed to register module container {}, as module loading has already begun", container);
            return;
        }
        Preconditions.checkNotNull(container);
        containers.put(container.getID(), container);
    }

    public void setup(ASMDataTable asmDataTable, File configDirectory) {
        currentStage = ModuleStage.M_SETUP;
        //configFolder = new File(configDirectory, GTValues.MODID);
        Map<String, List<IGregTechModule>> modules = getModules(asmDataTable);
        configureModules(modules);

        for (IGregTechModule module : loadedGCYLRModules) {
            currentContainer = containers.get(getContainerID(module));
            module.getLogger().debug("Registering event handlers");
            for (Class<?> clazz : module.getEventBusSubscribers()) {
                MinecraftForge.EVENT_BUS.register(clazz);
            }
        }
    }

    public void onConstruction(FMLConstructionEvent event) {
        currentStage = ModuleStage.CONSTRUCTION;
        for (IGregTechModule module : loadedGCYLRModules) {
            currentContainer = containers.get(getContainerID(module));
            module.getLogger().debug("Construction start");
            module.construction(event);
            module.getLogger().debug("Construction complete");
        }
    }

    public void onPreInit(FMLPreInitializationEvent event) {
        currentStage = ModuleStage.PRE_INIT;
        // Separate loops for strict ordering
        for (IGregTechModule module : loadedGCYLRModules) {
            currentContainer = containers.get(getContainerID(module));
            module.getLogger().debug("Registering packets");
            module.registerPackets();
        }
        for (IGregTechModule module : loadedGCYLRModules) {
            currentContainer = containers.get(getContainerID(module));
            module.getLogger().debug("Pre-init start");
            module.preInit(event);
            module.getLogger().debug("Pre-init complete");
        }
    }

    public void onInit(FMLInitializationEvent event) {
        currentStage = ModuleStage.INIT;
        for (IGregTechModule module : loadedGCYLRModules) {
            currentContainer = containers.get(getContainerID(module));
            module.getLogger().debug("Init start");
            module.init(event);
            module.getLogger().debug("Init complete");
        }
    }

    public void onPostInit(FMLPostInitializationEvent event) {
        currentStage = ModuleStage.POST_INIT;
        for (IGregTechModule module : loadedGCYLRModules) {
            currentContainer = containers.get(getContainerID(module));
            module.getLogger().debug("Post-init start");
            module.postInit(event);
            module.getLogger().debug("Post-init complete");
        }
    }

    public void onLoadComplete(FMLLoadCompleteEvent event) {
        currentStage = ModuleStage.FINISHED;
        for (IGregTechModule module : loadedGCYLRModules) {
            currentContainer = containers.get(getContainerID(module));
            module.getLogger().debug("Load-complete start");
            module.loadComplete(event);
            module.getLogger().debug("Load-complete complete");
        }
    }

    public void onServerStarting(FMLServerStartingEvent event) {
        currentStage = ModuleStage.SERVER_STARTING;
        for (IGregTechModule module : loadedGCYLRModules) {
            currentContainer = containers.get(getContainerID(module));
            module.getLogger().debug("Server-starting start");
            module.serverStarting(event);
            module.getLogger().debug("Server-starting complete");
        }
    }

    public void onServerStarted(FMLServerStartedEvent event) {
        currentStage = ModuleStage.SERVER_STARTED;
        for (IGregTechModule module : loadedGCYLRModules) {
            currentContainer = containers.get(getContainerID(module));
            module.getLogger().debug("Server-started start");
            module.serverStarted(event);
            module.getLogger().debug("Server-started complete");
        }
    }

    public void onServerStopped(FMLServerStoppedEvent event) {
        for (IGregTechModule module : loadedGCYLRModules) {
            currentContainer = containers.get(getContainerID(module));
            module.serverStopped(event);
        }
    }

    public void processIMC(ImmutableList<FMLInterModComms.IMCMessage> messages) {
        for (FMLInterModComms.IMCMessage message : messages) {
            for (IGregTechModule module : loadedGCYLRModules) {
                if (module.processIMC(message)) {
                    break;
                }
            }
        }
    }

    private void configureModules(Map<String, List<IGregTechModule>> modules) {
        //Locale locale = Locale.getDefault();
        //Locale.setDefault(Locale.ENGLISH);
        Set<ResourceLocation> toLoad = new HashSet<>();
        Set<IGregTechModule> modulesToLoad = new HashSet<>();
        //Configuration config = getConfiguration();

        for (IModuleContainer container : containers.values()) {
            String containerID = container.getID();
            List<IGregTechModule> containerModules = modules.get(containerID);
            //config.load();
            //config.addCustomCategoryComment(MODULE_CFG_CATEGORY_NAME,
            //        "Module configuration file. Can individually enable/disable modules from GregTech and its addons");
            IGregTechModule coreModule = getCoreModule(containerModules);
            if (coreModule == null) {
                throw new IllegalStateException("Could not find core module for module container " + containerID);
            } else {
                containerModules.remove(coreModule);
                containerModules.add(0, coreModule);
            }

            // Remove disabled modules and gather potential modules to load
            Iterator<IGregTechModule> iterator = containerModules.iterator();
            while (iterator.hasNext()) {
                IGregTechModule module = iterator.next();
                if (!isModuleEnabled(module)) {
                    iterator.remove();
                    logger.debug("Module disabled: {}", module);
                    continue;
                }
                GregTechModule annotation = module.getClass().getAnnotation(GregTechModule.class);
                toLoad.add(new ResourceLocation(containerID, annotation.moduleID()));
                modulesToLoad.add(module);
            }
        }

        // Check any module dependencies
        Iterator<IGregTechModule> iterator;
        boolean changed;
        do {
            changed = false;
            iterator = modulesToLoad.iterator();
            while (iterator.hasNext()) {
                IGregTechModule module = iterator.next();

                // Check module dependencies
                Set<ResourceLocation> dependencies = module.getDependencyUids();
                if (!toLoad.containsAll(dependencies)) {
                    iterator.remove();
                    changed = true;
                    GregTechModule annotation = module.getClass().getAnnotation(GregTechModule.class);
                    String moduleID = annotation.moduleID();
                    toLoad.remove(new ResourceLocation(moduleID));
                    logger.info("Module {} is missing at least one of module dependencies: {}, skipping loading...", moduleID, dependencies);
                }
            }
        } while (changed);

        // Sort modules
        do {
            changed = false;
            iterator = modulesToLoad.iterator();
            while (iterator.hasNext()) {
                IGregTechModule module = iterator.next();
                if (sortedGCYLRModules.keySet().containsAll(module.getDependencyUids())) {
                    iterator.remove();
                    GregTechModule annotation = module.getClass().getAnnotation(GregTechModule.class);
                    sortedGCYLRModules.put(new ResourceLocation(annotation.containerID(), annotation.moduleID()), module);
                    changed = true;
                    break;
                }
            }
        } while (changed);

        loadedGCYLRModules.addAll(sortedGCYLRModules.values());

        //if (config.hasChanged()) {
        //    config.save();
        //}
        //Locale.setDefault(locale);
    }

    private static IGregTechModule getCoreModule(List<IGregTechModule> modules) {
        for (IGregTechModule module : modules) {
            GregTechModule annotation = module.getClass().getAnnotation(GregTechModule.class);
            if (annotation.coreModule()) {
                return module;
            }
        }
        return null;
    }

    private static String getContainerID(IGregTechModule module) {
        GregTechModule annotation = module.getClass().getAnnotation(GregTechModule.class);
        return annotation.containerID();
    }

    private Map<String, List<IGregTechModule>> getModules(ASMDataTable table) {
        List<IGregTechModule> instances = getInstances(table);
        Map<String, List<IGregTechModule>> modules = new LinkedHashMap<>();
        for (IGregTechModule module : instances) {
            GregTechModule info = module.getClass().getAnnotation(GregTechModule.class);
            modules.computeIfAbsent(info.containerID(), k -> new ArrayList<>()).add(module);
        }
        return modules;
    }

    @SuppressWarnings("unchecked")
    private List<IGregTechModule> getInstances(ASMDataTable table) {
        Set<ASMDataTable.ASMData> dataSet = table.getAll(GregTechModule.class.getCanonicalName());
        List<IGregTechModule> instances = new ArrayList<>();
        for (ASMDataTable.ASMData data : dataSet) {
            String moduleID = (String) data.getAnnotationInfo().get("moduleID");
            List<String> modDependencies = (ArrayList<String>) data.getAnnotationInfo().get("modDependencies");
            if (modDependencies == null || modDependencies.stream().allMatch(Loader::isModLoaded)) {
                try {
                    Class<?> clazz = Class.forName(data.getClassName());
                    instances.add((IGregTechModule) clazz.newInstance());
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                    logger.error("Could not initialize module " + moduleID, e);
                }
            } else {
                logger.info("Module {} is missing at least one of mod dependencies: {}, skipping loading...", moduleID, modDependencies);
            }
        }
        return instances;
    }
}
