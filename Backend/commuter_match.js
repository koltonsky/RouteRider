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

async function getFirstEventsOfEachDay(userEmail) {
    try {
        await client.connect();

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
    } finally {
        await client.close();
    }
}


  // new function: finds other emails, excluding userEmail
  async function findOtherEmails(userEmail) {
    try {
        await client.connect();

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
    } finally {
        await client.close();
    }
}


async function findSchedule(userEmail) {
    try {
        await client.connect();

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
    } finally {
        await client.close();
    }
}

  


  //getFirstEventsOfEachDay("koltonluu@gmail.com");
  const userEmail = "koltonluu@gmail.com";
  //const getFirstEventsOfEachDayReturn = getFirstEventsOfEachDay(userEmail);

  //const emailsExcludingUser = findOtherEmails(userEmail);
  //console.log(emailsExcludingUser);
  //const findScheduleReturn = findSchedule(userEmail, emailsExcludingUser);

  
findSchedule(userEmail).then(result => {
    console.log('Schedule for other users:');
    console.log(result);
});

  //findMatchingCommuters("koltonluu@gmail.com");
  //findUsers("koltonluu")

module.exports = {
    //findUsers,
    getFirstEventsOfEachDay,
    findOtherEmails,

  };
  
