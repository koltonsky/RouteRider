const express = require('express');
// const fetch = require('node-fetch');
const { MongoClient} = require('mongodb');
// const ApiKeyManager = require('@esri/arcgis-rest-request');

const app = express()
app.use(express.json());

// MongoDB connection setup
const uri = 'mongodb://0.0.0.0:27017'; // Replace with your MongoDB connection string
const client = new MongoClient(uri);

// returns a list of users and events which match up on the first day
/*
async function findUsers(userEmail) {
    var userSchedule = await client.db('ScheduleDB').collection('schedulelist').findOne({email: userEmail});
    if (!userSchedule) {
        console.log('User not found or schedule is empty.');
        return;
      }
    
    console.log(userSchedule);

    var matchingEvents = [];
    return "print test";
}
*/
/**
 * Retrieves the first events of each day from a user's schedule.
 *
 * @param {string} userEmail - The email address of the user whose schedule is to be queried.
 * @returns {Object} An object containing an array of the first events of each day.
 *
 * This function performs the following steps:
 * 1. Retrieves the user's schedule from the database based on their email address.
 * 2. Checks if the user's schedule exists and is not empty; if not, it returns an empty array.
 * 3. Constructs an aggregation pipeline to find the first event of each day within the user's schedule.
 * 4. Executes the aggregation pipeline to retrieve the first events.
 * 5. Reverses the order of the first events to have the most recent first events at the beginning.
 * 6. Logs the first events of each day for reference.
 * 7. Returns an object containing the array of reversed first events.
 * 8. In case of an error, it returns an object with an empty events array.
 */
// ChatGPT usage: Yes
/*
async function getFirstEventsOfEachDay(userEmail) {
    try {

        const userSchedule = await client.db('ScheduleDB').collection('schedulelist').findOne({ email: userEmail });

        if (!userSchedule) {
            console.log('User not found or schedule is empty.');
        }

        // Aggregate the events to find the first event of each day
        const pipeline = [
            {
                $match: { email: userEmail },
            },
            {
                $unwind: '$events',
            },
            {
                $sort: { 'events.startTime': 1 }, // Sort events by start time in ascending order
            },
            {
                $group: {
                    _id: {
                        date: {
                            $dateToString: { format: '%Y-%m-%d', date: { $toDate: '$events.startTime' } }
                        },
                    },
                    event: { $first: '$events' },
                },
            },
        ];

        const firstEvents = await client.db('ScheduleDB').collection('schedulelist').aggregate(pipeline).toArray();
        const reversedFirstEvents = firstEvents.reverse();

        console.log('First events of each day:');
        console.log(reversedFirstEvents);

        return { events: reversedFirstEvents };
    } catch (err) {
        console.error('Error:', err);
    }
}
*/
async function getFirstEventsOfEachDay(userEmail) {
    //try {
 // Fetch the user schedule
 const userSchedule = await client.db('ScheduleDB').collection('schedulelist').findOne({ email: userEmail });

 if (!userSchedule || !userSchedule.events || userSchedule.events.length === 0) {
    //  console.log('User not found or schedule is empty.');
     //console.log(userSchedule.events);
     return { events: [] };
 }

 // Sort events by start time in ascending order
 const sortedEvents = userSchedule.events.sort((a, b) => new Date(a.startTime) - new Date(b.startTime));

 // Find the first event of each day
 const firstEvents = [];
 const seenDates = new Set();

 for (const event of sortedEvents) {
     const dateStr = new Date(event.startTime).toLocaleDateString();
     //console.log("DATESTR BE " + event.startTime);
     //.log("DATESTR " + dateStr);

     if (!seenDates.has(dateStr)) {
         seenDates.add(dateStr);
         firstEvents.push({ date: dateStr, event });
     }
 }

//  console.log('First events of each day:');
//  console.log(firstEvents);

 return { events: firstEvents };
    /*} catch (err) {
        console.error('Error:', err);
    }*/
}


/**
 * Finds and retrieves email addresses of schedules excluding the specified user's schedule.
 *
 * @param {string} userEmail - The email address of the user whose schedule is to be excluded.
 * @returns {Array} An array of email addresses belonging to other users' schedules.
 *
 * This function performs the following steps:
 * 1. Constructs an aggregation pipeline to filter out email addresses that are not the specified user's email.
 * 2. Executes the aggregation pipeline to find schedules belonging to other users.
 * 3. Extracts email addresses from the query result and stores them in an array.
 * 4. Logs the email addresses of schedules excluding the specified user for reference.
 * 5. Returns an array of email addresses.
 * 6. In case of an error, it returns an empty array.
 */
// ChatGPT usage: Yes
/*
  async function findOtherEmails(userEmail) {
    try {

        // Construct an aggregation pipeline to find emails of schedules except the user's schedule
        const pipeline = [
            {
                $match: { email: { $ne: userEmail } }
            },
            {
                $project: {
                    email: 1
                }
            }
        ];

        // Execute the aggregation pipeline
        const result = await client.db('ScheduleDB').collection('schedulelist').aggregate(pipeline).toArray();

        // Extract the email addresses from the result
        const emailsExcludingUser = result.map(schedule => schedule.email);

        console.log('Emails of schedules excluding the user:');
        console.log(emailsExcludingUser);

        return emailsExcludingUser;
    } catch (err) {
        console.error('Error:', err);
        return []; // Return an empty array in case of an error
    }
}
*/

async function findOtherEmails(userEmail) {
    //try {
        // Find schedules excluding the user's schedule
        const schedulesExcludingUser = await client
            .db('ScheduleDB')
            .collection('schedulelist')
            .find({ email: { $ne: userEmail } }, { projection: { email: 1 } })
            .toArray();

        // Extract the email addresses from the result
        const emailsExcludingUser = schedulesExcludingUser.map(schedule => schedule.email);

        //console.log('Emails of schedules excluding the user:');
        //console.log(emailsExcludingUser);

        return emailsExcludingUser;
    /*} catch (err) {
        console.error('Error:', err);
        return []; // Return an empty array in case of an error
    }*/
}


/**
 * Finds matching users based on the first events of the day for a given user.
 *
 * @param {string} userEmail - The email of the user to find matching users for.
 * @returns {Set} A Set of unique email addresses of users who have matching events.
 *
 * This function performs the following steps:
 * 1. Retrieves the first events of the day for the specified user.
 * 2. Retrieves a list of other email addresses, excluding the specified user's email.
 * 3. Compares the first events of the user with the first events of other users.
 * 4. If events have the same location (first 3 characters are 'UBC') and the same start time,
 *    the email address of the other user is added to the Set of matching users.
 * 5. Returns the Set of unique email addresses of users with matching events.
 * 6. In case of an error, it returns an empty array.
 */
// ChatGPT usage: Yes
/*
async function findMatchingUsers(userEmail) {
    try {

        // Step 1: Get the first events for the user
        const userFirstEvents = await getFirstEventsOfEachDay(userEmail);

        console.log('User First Events:');
        console.log(userFirstEvents);

        // Step 2: Find other emails, excluding the user's email
        const otherEmails = await findOtherEmails(userEmail);

        // Step 3: Create an empty list to hold the set of users
        const matchingUsers = new Set();

        for (const email of otherEmails) {
            const firstEventsOfTheDay = await getFirstEventsOfEachDay(email);

            console.log(`First Events of the Day for ${email}:`);
            console.log(firstEventsOfTheDay);

            // Step 4: Compare the events and add email to the list if conditions match
            for (const userEvent of userFirstEvents.events) {
                for (const otherEvent of firstEventsOfTheDay.events) {
                    
                    if (
                        userEvent.event.address.slice(0, 3) === 'UBC' &&
                        otherEvent.event.address.slice(0, 3) === 'UBC' &&
                        userEvent.event.startTime === otherEvent.event.startTime
                    ) {
                        matchingUsers.add(email);
                        break; // Once a match is found, no need to check other events for this email
                    }
                }
            }
        }

        console.log('Users with matching events:');
        console.log(matchingUsers);

        return matchingUsers;
    } catch (err) {
        console.error('Error:', err);
        return []; // Return an empty array in case of an error
    }
}
*/
async function findMatchingUsers(userEmail) {
    //try {
        // Step 1: Get the first events for the user
        const userFirstEvents = await getFirstEventsOfEachDay(userEmail);

        //console.log('User First Events:');
        //console.log(userFirstEvents);

        // Step 2: Find other emails, excluding the user's email
        const otherEmails = await findOtherEmails(userEmail);

        // Step 3: Create an empty list to hold the set of users
        const matchingUsers = new Set();

        // Step 4: Iterate through other emails and compare events
        for (const email of otherEmails) {
            // Get the first events of the day for the current email
            const firstEventsOfTheDay = await getFirstEventsOfEachDay(email);

            //console.log(`First Events of the Day for ${email}:`);
            //console.log(firstEventsOfTheDay);

            // Step 5: Compare the events and add email to the list if conditions match
            for (const userEvent of userFirstEvents.events) {
                for (const otherEvent of firstEventsOfTheDay.events) {
                    if (
                        userEvent.event.address.slice(0, 3) === 'UBC' &&
                        otherEvent.event.address.slice(0, 3) === 'UBC' &&
                        userEvent.event.startTime === otherEvent.event.startTime
                    ) {
                        matchingUsers.add(email);
                        break; // Once a match is found, no need to check other events for this email
                    }
                }
            }
        }

        //console.log('Users with matching events:');
        //console.log(matchingUsers);

        return matchingUsers;
    /*} catch (err) {
        console.error('Error:', err);
        return []; // Return an empty array in case of an error
    }*/
}


  


  //getFirstEventsOfEachDay("koltonluu@gmail.com");
  //const userEmail = "koltonluu@gmail.com";
  //const getFirstEventsOfEachDayReturn = getFirstEventsOfEachDay(userEmail);

  //const emailsExcludingUser = findOtherEmails(userEmail);
  //console.log(emailsExcludingUser);
  //const findScheduleReturn = findSchedule(userEmail, emailsExcludingUser);

/*
findMatchingUsers(userEmail).then(result => {
    console.log('Schedule for other users:');
    console.log(result);
});
*/

  //findMatchingCommuters("koltonluu@gmail.com");
  //findUsers("koltonluu")

module.exports = {
    //findUsers,
    getFirstEventsOfEachDay,
    findOtherEmails,
    findMatchingUsers,

  };
  
