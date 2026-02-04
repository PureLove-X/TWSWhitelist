# TWS Whitelist Plugin

This is a Discord-based whitelist plugin made specifically for **The Wooden Spoon**.  
The goal was to making whitelist handling more easily done for staff member and into a **clear, logged, staff-friendly Discord flow**.

This plugin is **very tightly** designed around how our server operates.

It currently works with the **Ticket Tool** Discord app and relies on how those tickets are structured. 

Because of that, **I would not recommend this plugin for general use** without modification.

---
## Important notes before using this

- This plugin is built **specifically for The Wooden Spoon**
- It assumes:
  - Ticket Tool is being used
  - Applications are created inside a specific category
  - The first message in the ticket mentions the applicant

If you’re not running a similar setup, expect to need changes.

---

## Application Features

Applications are handled entirely inside Discord ticket channels and include the following features:

### Automatic Staff Dashboard
- When a new application ticket is created, the plugin automatically posts a **staff dashboard**
- The dashboard appears shortly after the ticket opens so it doesn’t replace the Ticket Tool message
- The dashboard contains all staff interaction buttons in one place

### Button-Based Review Flow
- Staff interact using buttons instead of commands
- Available actions:
  - Approve
  - Deny
  - Request More Info
- Everyone can see the buttons, but only users with the configured **staff role** can interact with them

### IGN Detection
- The plugin attempts to automatically detect the applicant’s Minecraft username
- If found, the IGN is prefilled in approval modals
- Staff can still edit it if needed

### Approval Flow
- Clicking **Approve** opens a modal asking for the Minecraft username
- On success:
  - Player is whitelisted in Minecraft
  - Approved role is applied in Discord
  - Player receives the configured approval message
  - The action is logged

### Denial Flow
- Clicking **Deny** opens a dropdown of configurable denial reasons
- Reasons are fully configurable (up to 25, Discord limit)
- Selecting a reason:
  - Applies the denied role
  - Sends the corresponding denial message to the player
  - Logs the denial and selected reason

### Request More Info
- Sends a message to the applicant asking for additional information
- Does not apply or remove roles
- Still logs the action so staff know it happened

---

## Rewhitelist Features

This system is for players who were previously whitelisted and need to be added again.

### Player Submission
- Player posts their IGN in the rewhitelist channel
- The original message grabbed by the bot and reposted with a Confirm Rewhitelist button
- The player message is deleted but who requested the whitelist is preserved by the bot

### Staff Confirmation
- Staff click **Confirm Rewhitelist**
- A modal opens with the IGN prefilled
- On success:
  - Player is rewhitelisted in Minecraft
  - The embed updates to show whitelist status
  - The button is replaced with a disabled “Whitelisted” button
  - The action is logged

---

## Logging

Every whitelist-related action is logged in two places:

### Console
Provides a server-side record for auditing and debugging.

### Discord Log Channel
Logs include:
- Action type
- Minecraft username
- Staff member
- Source (application, rewhitelist, etc.)
- Timestamp

The message format is **not** currently configurable.

---

## Configuration

Everything is configured through YAML.

### Messages
- Messages are defined as lists (`-`) instead of block scalars to avoid Discord collapsing formatting
- Blank lines are supported using `""`  
- Placeholders can be used anywhere:
  - `{mention}` – Mentions the applicant on Discord (pings them)
  - `{applicant}` – The applicant’s Discord display name (no ping)
  - `{username}` – The Minecraft username being whitelisted
  - `{channel:CHANNEL_ID}` – Creates a clickable channel mention

### Denial Reasons
- Configurable list
- Used to populate the denial dropdown
- Limited to 25 options due to Discord restrictions

```yml
Bot:
  token: "PUT_BOT_TOKEN_HERE"
  server_id: "PUT_SERVER_ID_HERE"

Roles:
  staff_role_id: "PUT_ROLE_ID_HERE"
  #    Everyone can see the buttons but only users with this role will be able to interact with them
  approved_role_id: "PUT_ROLE_ID_HERE"
  #    The role you want people to get when approved
  denied_role_id: "PUT_ROLE_ID_HERE"
  #    The role you want people to get when denied
  noapp_role_id: "PUT_ROLE_ID_HERE"
  #    The role people have when making the application.

Channels:
  application_category_id: "PUT_CATEGORY_ID_HERE"
  #   The category whitelist applications are made in
  whitelist_log_channel_id: "PUT_CHANNEL_ID_HERE"
  #   The channel where you want whitelist logs to go to.
  rewhitelist_channel_id: "PUT_CHANNEL_ID_HERE"
  #    The channel where rewhitelist requests are posted

Messages:
  # Each '-' is a new line in that message. To skip a line do "".
  # I tried using a block scalar ( the | ) but I found that discord would collapse the messages
  # so until I can figure out a way around it this is the way I have to do it.
  Staff:
    approve:
      success:
        - "✅ **Whitelist successful**"
        - "🧍 Applicant: **{applicant}**"
        - "📝 Username: `{username}`"
      failed:
        - "❌ Whitelist failed."
        - "Invalid Minecraft username."
      error:
        - "❌ An internal error occurred while whitelisting."
      timeout:
        - "❌ Whitelist timed out. Try again."
    applicant_error:
      - "❌ Unable to determine the applicant."
      - "No user with the NoApp role is in this channel"

  Player:
    approved:
      - "{mention}"
      - ""
      - "🎉 **Welcome to The Wooden Spoon!** 🎉"
      - ""
      - "Your application has been **approved**, and you’re now whitelisted on the server."
      - ""
      - "Please make sure you’ve read the rules here:"
      - "👉 https://www.tws.gg/rules"
      - ""
      - "If you’d like, feel free to introduce yourself in {channel:1105818494689361951}."
      - "We’re glad to have you!"

    more_info:
      - "Hi, {mention}! Thanks for applying to **The Wooden Spoon** 🙂"
      - ""
      - "We just need a little more information before we can finish reviewing your application."
      - "Could you tell us a bit about yourself outside of Minecraft?"
      - ""
      - "Things like hobbies, interests, or what you enjoy doing in your free time are great."
      - ""
      - "There’s no right or wrong answer here, we just like to know the people behind the players 💛"

    denied_reasons:
      # These reasons pop up as a dropdown for staff to pick from. You can only add up to 25 messages.
      - label: "Under age requirement"
        message:
          - "Hi {mention}, thank you for applying, but unfortunately you don’t meet our age requirements"
          - "as we are an adults-only server."
          - ""
          - "You’re welcome to reapply once you meet the criteria."

      - label: "Low effort application"
        message:
          - "Thank you {mention}, for your interest in **The Wooden Spoon**."
          - ""
          - "Unfortunately, we've decided due to the low effort in the application"
          - "to reject your application."

      - label: "Not a good fit"
        message:
          - "{mention}"
          - ""
          - "Sorry, we’re going to deny your application as we don’t think you’re a good fit for our server."
          - ""
          - "We wish you luck on finding a server better suited for you!"
