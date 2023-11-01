const express = require('express');
const https = require('https');
const path = require('path');
const fs = require('fs');
const queryString = require('querystring');
const port = 8081;
// const fetch = require('node-fetch');
const { MongoClient} = require('mongodb');
// const ApiKeyManager = require('@esri/arcgis-rest-request');
const {ApiKeyManager} = require('@esri/arcgis-rest-request');
const {geocode} = require('@esri/arcgis-rest-geocoding');

const app = express()
app.use(express.json());
const axios = require('axios')

const user = require('./routes/user.js')
const schedule = require('./routes/schedule.js');
const { get } = require('http');
const { time } = require('console');

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
async function getFirstEventsOfEachDay(userEmail) {
    try {

        const userSchedule = await client.db('ScheduleDB').collection('schedulelist').findOne({ email: userEmail });

        if (!userSchedule) {
            console.log('User not found or schedule is empty.');
            return { events: [] };
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
        return { events: [] }; // Return an empty array in case of an error
    }
}

async function getAllEvents(userEmail) {
    try {
        const userSchedule = await client.db('ScheduleDB').collection('schedulelist').findOne({ email: userEmail });

        if (!userSchedule) {
            console.log('User not found or schedule is empty.');
            return { events: [] };
        }

        // Unwind the events array and sort by start time
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
        ];

        const events = await client.db('ScheduleDB').collection('schedulelist').aggregate(pipeline).toArray();

        //console.log('All events:');
        //console.log(events);

        return { events };
    } catch (err) {
        console.error('Error:', err);
        return { events: [] }; // Return an empty array in case of an error
    }
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

        //console.log('Emails of schedules excluding the user:');
        //console.log(emailsExcludingUser);

        return emailsExcludingUser;
    } catch (err) {
        console.error('Error:', err);
        return []; // Return an empty array in case of an error
    }
}

/**
 * Finds and retrieves users with matching events and their corresponding matching events.
 *
 * @param {string} userEmail - The email address of the user to find matching users for.
 * @returns {Object} A dictionary where each key is a user's email and the value is a list of matching events.
 *
 * This function performs the following steps:
 * 1. Retrieves the first events of the day for the specified user.
 * 2. Retrieves a list of other email addresses, excluding the specified user's email.
 * 3. Constructs a dictionary to store matching users and their corresponding matching events.
 * 4. Compares the first events of the user with the first events of other users.
 * 5. If events have the same 'UBC' address and the same start time, they are considered matching.
 * 6. Matching events are stored in an array with 'startTime' and 'address: UBC'.
 * 7. Entries are added to the dictionary, where the key is the user's email and the value is the list of matching events.
 * 8. Only users with matching events are included in the result.
 * 9. Returns a dictionary with user emails as keys and their matching events as values.
 * 10. In case of an error, it returns an empty object.
 */
async function findMatchingUsers(userEmail) {
    
    try {
        // Step 1: Get the first events for the user
        const userEvents = await getFirstEventsOfEachDay(userEmail);

        // Step 2: Find other emails, excluding the user's email
        const otherEmails = await findOtherEmails(userEmail);

        // Step 3: Create a dictionary to hold matching users and their matching events
        const matchingUsers = {};

        for (const email of otherEmails) {
            const otherUserEvents = await getFirstEventsOfEachDay(email);

            // Step 4: Compare the events and add user and matching events if conditions match
            const matchedEvents = [];
            for (const userEvent of userEvents.events) {
                for (const otherEvent of otherUserEvents.events) {
                    if (
                        userEvent.event.address.slice(0, 3) === 'UBC' &&
                        otherEvent.event.address.slice(0, 3) === 'UBC' &&
                        userEvent.event.startTime === otherEvent.event.startTime
                    ) {
                        matchedEvents.push({
                            startTime: userEvent.event.startTime,
                            address: 'UBC',
                        });
                        break; // Once a match is found, no need to check other events for this email
                    }
                }
            }

            if (matchedEvents.length > 0) {
                matchingUsers[email] = matchedEvents;
            }
        }

        return matchingUsers;
    } catch (err) {
        console.error('Error:', err);
        return {}; // Return an empty object in case of an error
    }
}


  


  //getFirstEventsOfEachDay("koltonluu@gmail.com");
  const userEmail = "koltonluu@gmail.com";
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
    getAllEvents,
    findOtherEmails,
    findMatchingUsers,

  };
  
