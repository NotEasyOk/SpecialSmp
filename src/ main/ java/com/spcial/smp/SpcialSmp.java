public final class SpcialSmp extends JavaPlugin {
    private PlayerDataManager playerData;
    private CooldownManager cooldowns;
    private Map<String, Card> cardRegistry;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.playerData = new PlayerDataManager(this);
        this.cooldowns = new CooldownManager();
        this.cardRegistry = new HashMap<>();
        registerCards();
        getServer().getPluginManager().registerEvents(new CardUseListener(this), this);
        getCommand("spcialcard").setExecutor(new CardCommand(this));
        getLogger().info("spcialSmp enabled");
    }

    private void registerCards() {
        cardRegistry.put("Enderman", new EndermanCard(this));
        // register other cards...
    }

    public Optional<Card> getCardByItem(ItemStack item) {
        // detect by persistent data or item meta.
    }
  }

