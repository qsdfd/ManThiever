import org.osbot.rs07.api.NPCS;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.RandomBehaviourHook;
import org.osbot.rs07.script.RandomEvent;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.awt.*;

@ScriptManifest(name = "Man Thiever", author = "dokato", version = 2.4, info = "", logo = "") 
public class MainManThiever extends Script {
	
	private static final Color standardTxtColor = new Color(255, 255, 255);
	private static final Color breakRectColor = new Color(0, 0, 0, 175);
	
	private static final Rectangle breakRect = new Rectangle(190, 110, 390, 50);
	
	private boolean startb = true;
	
    private long timeRan;
    private long timeBegan;
	private long timeBotted;
	private long timeOffline;
	
	private String status;
	
	private long timeLastBreaked;
	private long timeSinceLastBreaked;
	private long timeBreakStart;
	
	private static final long milisecondsPerMinute = 60000; 
	private static final long bottingTime = 52 * milisecondsPerMinute;
	private static final long breakingTime = 14 * milisecondsPerMinute;
	private static final long randomizeValue = 5 * milisecondsPerMinute;
	
	private long timeBotting;
	private long timeBreaing;
	
	private boolean resetBreakCheck = true;
	private boolean hasStarted = false;
	
	private boolean toBank;
	
	private boolean stunned;
	
	private static final Area DINER_AREA = new Area(3205,3226,3212,3218);

	private boolean done;
	
	@Override
    public void onStart(){
		this.timeBegan = System.currentTimeMillis();
		this.timeBotted = 0;
		this.timeOffline = 0;
		this.toBank = false;
		this.done = false;
		
		this.timeLastBreaked = System.currentTimeMillis();
		
		try {
		    this.bot.getRandomExecutor().registerHook(new RandomBehaviourHook(RandomEvent.AUTO_LOGIN) {
		        @Override
		        public boolean shouldActivate() {
		        	if(hasStarted && needToBreak()){
		        		status="Breaking";
		        		return false;
		        	}else{
		        		status="Loging in";
		        		return super.shouldActivate();
		        	}
		        }
		    });
		} catch (Exception ex) {
		    log("something went wrong");
		}
		
    }
    
    @Override
    public void onExit() {
    }


    @Override
    public int onLoop() throws InterruptedException{
    	status="loop started";
    	if(!needToBreak()){
	    	if(getClient().isLoggedIn()){
	    		breakTimeProcedures();
	    		if(getSkills().getStatic(Skill.THIEVING) < 46){
		    		procedures();
			    	checkToBank();
			    	if(!toBank){
			    		if(inThievPlace()){
			    			thiev();
			    		}else{
			    			goToThievPlace();
			    		}
			    	}else{
			    		if(inBank()){
			    			bank();
			    		}else{
			    			goToBank();
			    		}
			    	}
	    		}else{
	    			done = true;
	    			if(inDruidzArea())
	    				stop();
	    			else{
	    				if(toArdyRoad()){
	    					goToDruidzArea();
	    				}else if(!myPlayer().isAnimating()){
	    					status="need to tele to cammy";
	    					if(getInventory().contains("Camelot teleport")){
	    						if(getBank().isOpen()){
	    		    				getBank().close();
	    		    				sleep(random(200,500));
	    						}
	    						status="about to tele to cammy";
	    						if(!myPlayer().isAnimating()){
	    							status="teleing to cammy with tab";
	    							getInventory().getItem("Camelot teleport").interact("Break");
	    							sleep(random(900,1400));
	    						}
	    					}else{
	    						status="gonna get the cammy tab from bank";
	    						if(inBank()){
	    			    			bank();
	    			    		}else{
	    			    			goToBank();
	    			    		}
	    					}
	    				}
	    			}
	    		}
	    	}
    	}else{
			doBreak();
		}
    	
    	status="loop ended";
    	return 0;
    }


    @Override
    public void onPaint(Graphics2D g1){
    	
    	if(this.startb){
    		this.startb=false;
    		this.timeBegan=System.currentTimeMillis();
    	}
    	this.timeRan = (System.currentTimeMillis() - this.timeBegan);
    	this.timeSinceLastBreaked = System.currentTimeMillis() - this.timeLastBreaked;
		if (getClient().isLoggedIn()) {
			this.timeBotted = (this.timeRan - this.timeOffline);
		} else {
			this.timeOffline = (this.timeRan - this.timeBotted);
		}
		
		Graphics2D g = g1;

		int startY = 120;
		int increment = 15;
		int value = (-increment);
		int x = 20;
		
		g.setFont(new Font("Arial", 0, 13));
		g.setColor(standardTxtColor);
		g.drawString("Acc: " + getBot().getUsername().substring(0, getBot().getUsername().indexOf('@')), x,getY(startY, value+=increment));
		g.drawString("World: " + getWorlds().getCurrentWorld(),x,getY(startY, value+=increment));
		value+=increment;
		g.drawString("Version: " + getVersion(), x, getY(startY, value+=increment));
		g.drawString("Runtime: " + ft(this.timeRan), x, getY(startY, value+=increment));
		g.drawString("Time botted: " + ft(this.timeBotted), x, getY(startY, value+=increment));
		if(hasStarted)
			g.drawString("Last break: " + ft(this.timeSinceLastBreaked), x, getY(startY, value+=increment));
		g.drawString("Status: " + status, x, getY(startY, value+=increment));
		value+=increment;
		g.drawString("thiev lvl: " + getSkills().getStatic(Skill.THIEVING), x, getY(startY, value+=increment));
		g.drawString(""+getSkills().experienceToLevel(Skill.THIEVING), x, getY(startY, value+=increment));
		
		if(hasStarted && needToBreak()){
			g.setColor(breakRectColor);
			fillRect(g, breakRect);
			g.setColor(standardTxtColor);
			g.drawString("Have to break for: " + ft(this.timeBreaing) , 275, 130);
			g.drawString("Have been breaking for: " + ft((System.currentTimeMillis() - this.timeBreakStart)), 275, 145);
		}
    }
    
    private int getY(int startY, int value){
		return startY + value;
	}
    
    private void fillRect(Graphics2D g, Rectangle rect){
		g.fillRect(rect.x, rect.y, rect.width, rect.height);
	}
    
    public void onMessage(Message message) throws InterruptedException {
		String txt = message.getMessage().toLowerCase().trim();
		
		if(txt.contains("been stunned")){
			this.stunned=true;
		}
    }
    
    private boolean needToBreak(){
		status="returning if i need to break";
		return (timeSinceLastBreaked > this.timeBotting) && (timeSinceLastBreaked < (this.timeBotting + this.timeBreaing));
	}
	
    private void doBreak() throws InterruptedException{
		status="Have to break";
		if(getClient().isLoggedIn()){
			resetBreakCheck=true;
			status="logging out to break";
			getLogoutTab().logOut();
			sleep(random(1000,1600));
			this.timeBreakStart = System.currentTimeMillis();
		}
	}
	
	private void breakTimeProcedures(){
		status="break time procedures";
		if(resetBreakCheck){
			resetBreakCheck=false;
			this.timeLastBreaked = System.currentTimeMillis();
			
			this.timeBotting = getBottingTime();
			this.timeBreaing = getBreakingTime();
			
			log("After " + ft(this.timeBotting) + " gonna break for " + ft(this.timeBreaing));
		}
		this.hasStarted = true;
	}
	
	private long getBottingTime(){
		status="getting bottingTime";
		return this.bottingTime + getRandomBreakValue();
	}
	
	private long getBreakingTime(){
		status="getting breakingTime";
		return this.breakingTime + getRandomBreakValue();
	}
	
	private long getRandomBreakValue(){
		status="getting random break value";
		return  ThreadLocalRandom.current().nextLong(-randomizeValue, randomizeValue);
	}
    
    private void goToDruidzArea(){
    	status="Walking to druidz Area";
    	if(getMap().canReach(new Position(2617,3337,0)))
			getWalking().walk(new Area(2617,3337,2619,3337));
    	else if(getMap().canReach(new Position(2623,3337,0)))
			getWalking().walk(new Area(2623,3338,2627,3335));
    	else if(getMap().canReach(new Position(2629,3335,0)))
			getWalking().walk(new Area(2629,3335,2631,3337));
    	else if(getMap().canReach(new Position(2636,3338,0)))
			getWalking().walk(new Area(2636,3338,2634,3343));
    	else if(getMap().canReach(new Position(2636,3347,0)))
			getWalking().walk(new Area(2636,3347,2635,3350));
    	else if(getMap().canReach(new Position(2637,3356,0)))
			getWalking().walk(new Area(2637,3356,2636,3361));
    	else if(getMap().canReach(new Position(2637,3365,0)))
			getWalking().walk(new Area(2637,3365,2636,3369));
    	else if(getMap().canReach(new Position(2636,3372,0)))
			getWalking().walk(new Area(2636,3372,2640,3374));
    	else if(getMap().canReach(new Position(2646,3377,0)))
			getWalking().walk(new Area(2646,3377,2647,3382));
    	else if(getMap().canReach(new Position(2650,3385,0)))
			getWalking().walk(new Area(2650,3385,2655,3388));
    	else if(getMap().canReach(new Position(2656,3392,0)))
			getWalking().walk(new Area(2656,3392,2661,3394));
    	else if(getMap().canReach(new Position(2665,3396,0)))
			getWalking().walk(new Area(2665,3396,2670,3398));
    	else if(getMap().canReach(new Position(2673,3399,0)))
			getWalking().walk(new Area(2673,3399,2677,3401));
    	else if(getMap().canReach(new Position(2680,3404,0)))
			getWalking().walk(new Area(2680,3404,2685,3411));
    	else if(getMap().canReach(new Position(2687,3411,0)))
			getWalking().walk(new Area(2687,3411,2689,3417));
    	else if(getMap().canReach(new Position(2690,3418,0)))
			getWalking().walk(new Area(2690,3418,2692,3423));
    	else if(getMap().canReach(new Position(2694,3423,0)))
			getWalking().walk(new Area(2694,3423,2698,3428));
    	else if(getMap().canReach(new Position(2701,3427,0)))
			getWalking().walk(new Area(2701,3427,2705,3432));
    	else if(getMap().canReach(new Position(2706,3432,0)))
			getWalking().walk(new Area(2706,3432,2708,3437));
    	else if(getMap().canReach(new Position(2708,3438,0)))
			getWalking().walk(new Area(2708,3438,2711,3442));
    	else if(getMap().canReach(new Position(2710,3443,0)))
			getWalking().walk(new Area(2710,3443,2708,3446));
    	else if(getMap().canReach(new Position(2713,3448,0)))
			getWalking().walk(new Area(2713,3448,2712,3453));
    	else if(getMap().canReach(new Position(2715,3453,0)))
			getWalking().walk(new Area(2715,3453,2720,3455));
    	else if(getMap().canReach(new Position(2720,3456,0)))
			getWalking().walk(new Area(2720,3456,2723,3461));
    	else if(getMap().canReach(new Position(2724,3461,0)))
			getWalking().walk(new Area(2724,3461,2728,3464));
    	else if(getMap().canReach(new Position(2723,3466,0)))
			getWalking().walk(new Area(2723,3466,2721,3470));
    	else if(getMap().canReach(new Position(2723,3471,0)))
			getWalking().walk(new Area(2723,3471,2726,3476));
    	else if(getMap().canReach(new Position(2727,3476,0)))
			getWalking().walk(new Area(2727,3476,2731,3479));
    	else if(getMap().canReach(new Position(2731,3477,0)))
			getWalking().walk(new Area(2731,3477,2737,3478));
    	else if(getMap().canReach(new Position(2739,3480,0)))
			getWalking().walk(new Area(2739,3480,2744,3477));
    	else if(getMap().canReach(new Position(2746,3479,0)))
			getWalking().walk(new Area(2746,3479,2749,3475));
    	else if(getMap().canReach(new Position(2751,3478,0)))
			getWalking().walk(new Area(2751,3478,2754,3477));
    }
    
    private boolean toArdyRoad(){
    	status="Returning toArdyRoad";
    	return myPosition().getX() < 2770;
    }
    
    private boolean inDruidzArea(){
    	status="Returning in druidz area";
    	return myPosition().getX() < 2626;
    }
    
    private void bank() throws InterruptedException{
    	status="about to bank";
    	if(getBank().isOpen()){
    		if(done){
    			status="gonna withdraw the cammy tab from bank";
    			if(getBank().contains("Camelot teleport")){
    				status="withdrawing cammy tab from bank";
    				getBank().withdraw("Camelot teleport", 1);
    				sleep(random(200,600));
    			}
    		}else{
	    		status="depositing all";
	    		getBank().depositAll();
	    		sleep(random(600,900));
	    		if(getInventory().isEmpty()){
	    			status="setting toBank to false";
	    			toBank=false;
	    		}
    		}
    	}else if(!myPlayer().isMoving()){
    		status="interacting with booth";
    		objects.closest(18491/*bank booth*/).interact("Bank");
    		sleep(random(400,700));
    	}
    }
    
    private void goToBank() throws InterruptedException{
    	status="going to bank";
    	
    	if(!myPlayer().isMoving()){
    		if(getMap().canReach(objects.closest("Staircase"))){
	    		status="walking to staircase";
	    		getWalking().walk(objects.closest("Staircase"));
				status="climbing up ";
	    		objects.closest("Staircase").interact("Climb-up");
	    		sleep(random(200,400));
    		}else{
    			openDoor(null);
    		}
		}
    }
    
    private boolean inBank(){
    	status="returning inBank";
    	return myPosition().getZ()==2;
    }
    
    private void thiev() throws InterruptedException{
    	status = "gonna get a thievable NPC";
    	NPC toThiev = getToThiev();//getNpcs().closest("Man","Woman");
    	status="about to thiev men";
    	if(getMap().canReach(toThiev)){
    		if(!myPlayer().isMoving() && !myPlayer().isAnimating() && !myPlayer().isUnderAttack() && !toThiev.isAnimating()){
        		status="Thieving man or woman";
        		toThiev.interact("Pickpocket");
        		if(stunned){
        			status="you've been stunned!, wait 3 sec";
        			sleep(random(3000,3500));
        		}else{
        			status="wait a bit";
        			sleep(random(1500,2100));
        		}
        		stunned = false;
        	}else{

    			status="Waiting";
        		wait();
        	}
    	}else{
    		openDoor(toThiev);
    	}
    }
    
    private NPC getToThiev(){
    	for(NPC npc : getNpcs().getAll()){
    		status="checking npc conditions";
    		if((npc.hasAction("Pickpocket") && npc.getId() != 6288) && !inSpinArea(npc.getPosition()))
    			return npc;
    	}
    	return null;
    }
    
    private void openDoor(NPC toThiev) throws InterruptedException{
    	RS2Object door;
    	status="checking door conditions";
    	if(inSpinArea(myPosition()) ||
    			((inDukeArea(myPosition()) && toBank) ||
    			(inDukeArea(myPosition()) && !inDukeArea(toThiev.getPosition()) ||
    					(toThiev != null && ((inDukeArea(toThiev.getPosition()) && !inDukeArea(myPosition())) && !inBalconArea(myPosition()))))))
    		door = getDoor(1543);
    	else
    		door = getDoor(1535);
    	
    	status="opening door";
    	door.interact("Open");
    	sleep(random(500,900));
    }
    
    private RS2Object getDoor(int id){
    	for(RS2Object obj : getObjects().getAll())
    		if(obj.getId() == id && getMap().canReach(obj))
    			if(obj.getId() == 1535)
    				return getObjects().closest(obj.getId());
    			else
    				return obj;
    	log("couldn't get good door");
    	return null;
    }
    
    private boolean inBalconArea(Position pos){
    	status = "returning in balcon area";
    	return pos.getY() >= 3226
    			&& pos.getY() <= 3211
    			&& pos.getX() >= 3214;
    }
    
    private boolean inSpinArea(Position pos){
    	status = "returning in spin area";
    	return pos.getX() <= 3213 
    			&& pos.getX() >= 3208
    			&& pos.getY() <= 3217
    			&& pos.getY() >= 3212;
    }
    
    private boolean inDukeArea(Position pos){
    	status = "returning in duke area";
    	return pos.getX() <= 3213 
    			&& pos.getX() >= 3208
    			&& pos.getY() <= 3225
    			&& pos.getY() >= 3218;
    }
    
    private void goToThievPlace() throws InterruptedException{
    	status="entered goTothievPlace";
    	if(DINER_AREA.contains(myPlayer()) && myPosition().getZ()==0){
    		status="opening large door";
    		objects.closest("Large door").interact("Open");
    		sleep(random(600,900));
    	}else if(myPosition().getZ() == 2){
    		status="about to  climb down";
    		if(!myPlayer().isMoving()){
    			status="walking to staircase";
    			getWalking().walk(objects.closest("Staircase"));
    			status="climbing down";
	    		objects.closest("Staircase").interact("Climb-down");
	    		sleep(random(200,400));
    		}
    	}else if(myPosition().getZ() == 0){
    		status="about to  climb up";
    		if(!myPlayer().isMoving()){
        		status="walking to staircase";
        		getWalking().walk(objects.closest("Staircase"));
    			status="climbing up ";
        		objects.closest("Staircase").interact("Climb-up");
        		sleep(random(200,400));
    		}
    	}
    }
    
    private boolean inThievPlace(){
    	status="returning inThievPlace";
    	return myPosition().getZ()==1;
    }
    
    private void checkToBank(){
    	status="checktobank 1";
    	if(!toBank){
    		status="checktobank 2";
	    	if(getSkills().getDynamic(Skill.HITPOINTS)<5 && getInventory().contains("Coins")){
	    		status="checktobank 3";
	    		if(getInventory().getItem("Coins").getAmount()>200){
	    			status="checktobank 4";
	    			toBank=true;
	    		}
	    	}
    	}
    }
    
    private void procedures() throws InterruptedException{
    	getCamera().toTop();
		if(getInventory().isItemSelected()){
			getInventory().deselectItem();
			sleep(random(200,400));
		}
		if(getSettings().getRunEnergy()>random(7,14)){
			getSettings().setRunning(true);
			sleep(random(200,400));
		}
	}

	private String ft(long duration) {
		String res = "";
		long days = TimeUnit.MILLISECONDS.toDays(duration);
		long hours = TimeUnit.MILLISECONDS.toHours(duration)
				- TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));
		long minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
				- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
						.toHours(duration));
		long seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
				- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
						.toMinutes(duration));
		if (days == 0L) {
			res = hours + ":" + minutes + ":" + seconds;
		} else {
			res = days + ":" + hours + ":" + minutes + ":" + seconds;
		}
		return res;
	}
}