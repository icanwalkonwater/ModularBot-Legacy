package com.jesus_crie.modularbot.utils.menu;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.Checks;

public class MenuBuilder {

    private final Message target;
    private User userTarget;
    private long timeout = 0;
    private boolean deleteAfter = false;

    /**
     * Initialize the builder with the message to bind to.
     * @param bindTo the message to bind.
     */
    public MenuBuilder(Message bindTo) {
        Checks.notNull(bindTo, "message");
        target = bindTo;
    }

    /**
     * Specify the user who will have the right to use the menu.
     * @param t the targeted user.
     * @return the current builder.
     */
    public MenuBuilder targetUser(User t) {
        Checks.notNull(t, "user");
        userTarget = t;
        return this;
    }

    /**
     * Specify a timeout in millisecond, after this time the menu will be destroyed.
     * By default it's infinite.
     * @param timeout the timeout in milliseconds.
     * @return the current builder.
     */
    public MenuBuilder useTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * With this, the menu will be deleted when the timeout is reached or when an action is selected.
     * @return the current builder.
     */
    public MenuBuilder deleteAfter() {
        deleteAfter = true;
        return this;
    }

    /**
     * Build a {@link ModularMenu} based on the parameters given before.
     * The message and the user must not be null.
     *
     * Remember to run the menu with {@link ModularMenu#run()}.
     * @return a new instance of {@link ModularMenu}.
     */
    public ModularMenu build() {
        Checks.notNull(target, "message");
        Checks.notNull(userTarget, "user");
        return new ModularMenu(target,
                userTarget,
                timeout,
                deleteAfter);
    }
}
