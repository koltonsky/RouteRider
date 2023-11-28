const { MongoClient } = require('mongodb');

const supertest = require('supertest');
const { app, stopSSLServer } = require('../server'); // Replace with the actual path to your Express app
const request = supertest(app);

const { findUserToken, checkLiveTransitTime } = require('../server');
const mockSendNotification = jest.fn(() => 1);
const mockSendNotification2 = jest.fn(() => 0);

beforeAll(async () => {
    // Set up MongoDB connection before tests
    try {
      const uri = 'mongodb://0.0.0.0:27017'; // Replace with your MongoDB connection string
      client = new MongoClient(uri);
      await client.connect();
      console.log("connected");
    } catch (error) {
      console.error('MongoDB Connection Error:', error);
    }
});
  
afterAll(async () => {
    // Close MongoDB connection after all tests
    if (client) {
        await client.close();
    }
    //closeServer();
    stopSSLServer();
});
  
beforeEach(() => {
    // Log messages or perform setup before each test if needed
    // Avoid logging directly in beforeAll for async operations
});

// Interface POST https://20.163.28.92:8081/api/store_token
describe('Create/update firebasetokens for mobile notifications', () => {
    const userData = {
        email: 'userserver@example.com',
        token: '1234567890'
    };
  
    // Set up the test data before running the tests
    beforeAll(async () => {
      const collection = client.db('UserDB').collection('userlist');
      await collection.insertOne(userData);
    });
    // Clean up the test data after running all the tests
    afterAll(async () => {
      // Assuming you have already connected to the MongoDB client
      const collection = client.db('UserDB').collection('userlist');
      await collection.deleteOne({ email: userData.email });
    });

    // Input: req.body.email matches a user in the database and req.body.token is not null
    // Expected status code: 200
    // Expected behavior: database is updated with the new token 
    // ChatGPT usage: Yes
    test('Matching email and valid token', async () => {
        const response = await request.post('/api/store_token').send({
            email: 'userserver@example.com', 
            token: '54321'
        });
        expect(response.statusCode).toBe(200);
        expect(response.body).toEqual({message: 'fcmToken updated successfully'});
    });

    // Input: req.body.email does not match a user in the database and req.body.token is not null
    // Expected status code: 400
    // Expected behavior: database is unchanged 
    // ChatGPT usage: Yes
    test('Non-existing email and valid token', async () => {
        const response = await request.post('/api/store_token').send({
            email: 'user1@example.com', 
            token: '54321'
        });
        expect(response.statusCode).toBe(400);
        expect(response.body).toEqual({message: 'User not found, failed to update fcmToken'});
    });

    // Input: req.body.email and req.body.token are null
    // Expected status code: 400
    // Expected behavior: database is unchanged 
    // ChatGPT usage: Yes
    test('Null email and token', async () => {
        const response = await request.post('/api/store_token').send({
            email: null, 
            token: null
        });
        expect(response.statusCode).toBe(400);
        expect(response.body).toEqual({message: 'Invalid email or token'});
    });
});

// Interface POST https://20.163.28.92:8081/api/send-friend-notification
describe('Send friend request notifications', () => {
    const senderName = 'sender name';
    const receiverData = {
        email: 'receiver@example.com',
        fcmToken: '0987654321'
    };

    // Set up the test data before running the tests
    beforeAll(async () => {
      const collection = client.db('UserDB').collection('userlist');
      await collection.insertOne(receiverData);
    });
    // Clean up the test data after running all the tests
    afterAll(async () => {
      // Assuming you have already connected to the MongoDB client
      const collection = client.db('UserDB').collection('userlist');
      await collection.deleteOne({ email: receiverData.email });
    });

    // Input: provided email does not match a user in the database
    // Expected status code: 400
    // Expected behavior: notification is not sent and an error message is returned
    // ChatGPT usage: Yes
    test('email with no database entry', async () => {
        const response = await findUserToken("non-exist@emample.com", senderName, mockSendNotification);
        expect(mockSendNotification).not.toHaveBeenCalled();
        expect(response.status).toBe(400);
        expect(response.message).toEqual('Receiver not found, failed to send notification');
    });

    // Input: provided email matches a user in the database
    // Expected status code: 200
    // Expected behavior: a friend request notification is sent to the specified user and a success message is returned
    // ChatGPT usage: Yes
    test('matching email found in database', async () => {
        const response = await findUserToken(receiverData.email, senderName, mockSendNotification);
        expect(mockSendNotification).toHaveBeenCalled();
        expect(response.status).toBe(200);
        expect(response.message).toEqual('Successfully sent friend request notification');
    });

    // Input: provided email is null
    // Expected status code: 400
    // Expected behavior: notification is not sent and an error message is returned
    // ChatGPT usage: Yes
    test('null email or sender name', async () => {
        const response = await findUserToken(null, senderName, mockSendNotification);
        expect(response.status).toBe(400);
        expect(response.message).toEqual('Null receiver email or sender name');
    });

    // Input: provided email has invalid fcmToken
    // Expected status code: 400
    // Expected behavior: notification is not sent and an error message is returned
    // ChatGPT usage: Yes
    test('null email or sender name', async () => {
        const response = await findUserToken(receiverData.email, senderName, mockSendNotification2);
        expect(mockSendNotification2).toHaveBeenCalled();
        expect(response.status).toBe(400);
        expect(response.message).toEqual('Failed to send friend request notification');
    });

    // Input: provided email matches a user in the database
    // Expected status code: 400
    // Expected behavior: a friend request notification is sent to the specified user and a success message is returned
    // ChatGPT usage: Yes
    test('matching email found in database, api call', async () => {
        const response = await request.post('/api/send-friend-notification').send({
            senderName: senderName,
            receiverEmail: "bad email"
        });
        
        expect(response.status).toBe(400);
    });
});

// Interface POST https://20.163.28.92:8081/api/initReminders
describe('Initialize time to leave reminders', () => {
    const dummy_schedule = {
        email: 'dummy@example.com',
        events: [
            {
            address: 'UBC MacLeod, Room 4006',
            calendarID: 'dummy@example.com',
            endTime: '2023-12-01T15:00:00.000-07:00',
            eventName: 'CPEN 321 L1B',
            geolocation: { latitude: 0, longitude: 0 },
            id: '_64p36d1h6osjgchm6cp3gchk68r62oj3cgp3ge9h6krg_20231101T200000Z',
            startTime: '2023-12-01T13:00:00.000-07:00',
            }
        ]
    };
    const dummy_user = {
        email: 'dummy@example.com',
        name: 'dummy dumb',
        address: '5870 Rumble Street, Burnaby, BC',
        friends: ['friend1@example.com', 'friend2@example.com'],
        fcmToken: '1234567890'
    };
    const dummy_user1 = {
        email: 'dummy2@example.com',
    };

    const dummy_schedule123 = {
        email: 'dummy123@example.com',
        events: [
            {
            address: 'UBC MacLeod, Room 4006',
            calendarID: 'dummy@example.com',
            endTime: '2023-12-01T15:00:00.000-07:00',
            eventName: 'CPEN 321 L1B',
            geolocation: { latitude: 0, longitude: 0 },
            id: '_64p36d1h6osjgchm6cp3gchk68r62oj3cgp3ge9h6krg_20231101T200000Z',
            startTime: '2023-12-01T13:00:00.000-07:00',
            }
        ]
    };
    const dummy_user123 = {
        email: 'dummy123@example.com',
        name: 'dummy dumb',
        address: 'Royal Oak Station, Burnaby, BC',
        friends: ['friend1@example.com', 'friend2@example.com'],
    };
  
    // Set up the test data before running the tests
    beforeAll(async () => {
        await client.db('ScheduleDB').collection('schedulelist').insertOne(dummy_schedule)
        await client.db('ScheduleDB').collection('schedulelist').insertOne(dummy_schedule123)
        await client.db('UserDB').collection('userlist').insertOne(dummy_user)
        await client.db('UserDB').collection('userlist').insertOne(dummy_user1)
        await client.db('UserDB').collection('userlist').insertOne(dummy_user123)
    });
    // Clean up the test data after running all the tests
    afterAll(async () => {
        await client.db('ScheduleDB').collection('schedulelist').deleteOne(dummy_schedule)
        await client.db('ScheduleDB').collection('schedulelist').deleteOne(dummy_schedule123)
        await client.db('UserDB').collection('userlist').deleteOne(dummy_user)
        await client.db('UserDB').collection('userlist').deleteOne(dummy_user1)
        await client.db('UserDB').collection('userlist').deleteOne(dummy_user123)
    });

    // Input: provided email matches a user in the database
    // Expected status code: 200
    // Expected behavior: reminders of when to catch the bus are scheduled for the specified user and a success message is returned
    // ChatGPT usage: Yes
    test('matching email found in database', async () => {
        const response = await request.post('/api/initReminders').send({
            email: "dummy@example.com"
        });
        expect(response.statusCode).toBe(200);
        expect(response.body).toEqual({message: 'Reminders initialized'});
    });

    // Input: provided email matches a user in the database, address of user is right next to a skytrain station
    // Expected status code: 200
    // Expected behavior: reminders of when to catch the bus are scheduled for the specified user and a success message is returned
    // ChatGPT usage: Yes
    test('matching email found in database', async () => {
        const response = await request.post('/api/initReminders').send({
            email: "dummy123@example.com"
        });
        expect(response.statusCode).toBe(200);
        expect(response.body).toEqual({message: 'Reminders initialized'});
    });

    // Input: provided email doesn't match a user in the database
    // Expected status code: 400
    // Expected behavior: no reminders are scheduled and an error message is returned
    // ChatGPT usage: Yes
    test('no matching emails found in database', async () => {
        const response = await request.post('/api/initReminders').send({
            email: "dummy1@example.com"
        });
        expect(response.statusCode).toBe(400);
        expect(response.body).toEqual({message: 'No matching email exists in user database'});
    });

    // Input: provided email matches a user in the database, but has no associated schedule
    // Expected status code: 400
    // Expected behavior: no reminders are scheduled and an error message is returned
    // ChatGPT usage: Yes
    test('schedule not found in database', async () => {
        const response = await request.post('/api/initReminders').send({
            email: "dummy2@example.com"
        });
        expect(response.statusCode).toBe(400);
        expect(response.body).toEqual({message: 'No schedule associated with email'});
    });

    // Input: user email that has valid fcmToken, valid bus numbers
    // Expected behaviour: notification is sent to user and returns true 
    // ChatGPT usage: No
    test('test checkLiveTransitTime', async () => {
        const userEmail = 'dummy@example.com';
        const busNumber = '123';
        const stopNumber = '51439';
        const scheduledLeaveTime = '2023-12-01T14:00:00.000-07:00';
        const notificationCallback = mockSendNotification;
        const response = await checkLiveTransitTime(  
            userEmail,
            busNumber,
            stopNumber,
            scheduledLeaveTime,
            notificationCallback
        );

        expect(response).toBe(true);
    });

    // Input: user email that has invalid fcmToken, valid bus numbers
    // Expected behaviour: no notification is sent and returns false 
    // ChatGPT usage: No
    test('test checkLiveTransitTime, failed to send notification', async () => {
        const userEmail = 'dummy@example.com';
        const busNumber = '123';
        const stopNumber = '51439';
        const scheduledLeaveTime = '2023-12-01T14:00:00.000-07:00';
        const notificationCallback = mockSendNotification2;
        const response = await checkLiveTransitTime(  
            userEmail,
            busNumber,
            stopNumber,
            scheduledLeaveTime,
            notificationCallback
        );

        expect(response).toBe(false);
    });

    // Input: user email that has valid fcmToken, invalid bus numbers
    // Expected behaviour: no notification is sent and returns false 
    // ChatGPT uasge: No
    test('test checkLiveTransitTime, bad inputs', async () => {
        const userEmail = 'dummy@example.com';
        const busNumber = '123';
        const stopNumber = '51412312339';
        const scheduledLeaveTime = '2023-12-01T14:00:00.000-07:00';
        const notificationCallback = mockSendNotification2;
        const response = await checkLiveTransitTime(  
            userEmail,
            busNumber,
            stopNumber,
            scheduledLeaveTime,
            notificationCallback
        );

        expect(response).toBe(false);
    });
});

// Interface GET https://20.163.28.92:8081/api/recommendation/routesWithFriends/:email/:friendEmail/:date
describe('Create a route shared between two users', () => {
    const dummy_schedule = {
        email: 'dummy@example.com',
        events: [
            {
                address: 'UBC Nest Building, Room 360',
                calendarID: 'koltonluu@gmail.com',
                endTime: '2023-12-03T17:00:00.000-07:00',
                eventName: 'Club Meeting',
                geolocation: { latitude: 0, longitude: 0 },
                id: '_64p36d1h6osjgchm6cp3gchk68r62oj3cgpj4d1m64tr_20231101T223000Z',
                startTime: '2023-12-03T15:30:00.000-07:00',
            },
            {
                address: 'UBC Nest Building, Room 360',
                calendarID: 'koltonluu@gmail.com',
                endTime: '2023-12-13T17:00:00.000-07:00',
                eventName: 'Club Meeting',
                geolocation: { latitude: 0, longitude: 0 },
                id: '_64p36d1h6osjgchm6cp3gchk68r62oj3cgpj4d1m64tr_20231101T223000Z',
                startTime: '2023-12-13T15:30:00.000-07:00',
            },
        ],
    };
    const dummy_user = {
        email: 'dummy@example.com',
        name: 'John Doe',
        address: '5870 Rumble Street, Burnaby, BC',
        friends: ['friend1@example.com', 'friend2@example.com'],
    };

    const dummy_schedule2 = {
        email: 'dummy2@example.com',
        events: [
            {
                address: 'UBC Nest Building, Room 360',
                calendarID: 'leonguo@gmail.com',
                endTime: '2023-12-03T17:00:00.000-07:00',
                eventName: 'Club Meeting',
                geolocation: { latitude: 0, longitude: 0 },
                id: '_64p36d1h6osjgchm6cp3gchk68r62oj3cgpj4d1m64tr_20231101T223000Z',
                startTime: '2023-12-03T14:30:00.000-07:00',
            },
            {
                address: 'UBC Nest Building, Room 360',
                calendarID: 'leonguo@gmail.com',
                endTime: '2023-12-23T17:00:00.000-07:00',
                eventName: 'Club Meeting',
                geolocation: { latitude: 0, longitude: 0 },
                id: '_64p36d1h6osjgchm6cp3gchk68r62oj3cgpj4d1m64tr_20231101T223000Z',
                startTime: '2023-12-23T14:30:00.000-07:00',
            },
        ],
    };
    const dummy_user2 = {
        email: 'dummy2@example.com', // fake email, wont work with google authentication
        name: 'Leon Guo',
        address: '7746 Berkley Street, Burnaby, BC',
        friends: ['friend1@example.com', 'friend2@example.com'],
    };
    const dummy_user3 = {
        email: 'dummy3@example.com', // fake email, wont work with google authentication
        name: 'Leon Guo',
        address: '7746 Berkley Street, Burnaby, BC',
        friends: ['friend1@example.com', 'friend2@example.com'],
    };

    const dummy_schedule4 = {
        email: 'dummy4@example.com',
        events: [
            {
                address: 'UBC Nest Building, Room 360',
                calendarID: 'koltonluu@gmail.com',
                endTime: '2023-12-03T17:00:00.000-07:00',
                eventName: 'Club Meeting',
                geolocation: { latitude: 0, longitude: 0 },
                id: '_64p36d1h6osjgchm6cp3gchk68r62oj3cgpj4d1m64tr_20231101T223000Z',
                startTime: '2023-12-03T15:30:00.000-07:00',
            },
            {
                address: 'UBC Nest Building, Room 360',
                calendarID: 'koltonluu@gmail.com',
                endTime: '2023-12-13T17:00:00.000-07:00',
                eventName: 'Club Meeting',
                geolocation: { latitude: 0, longitude: 0 },
                id: '_64p36d1h6osjgchm6cp3gchk68r62oj3cgpj4d1m64tr_20231101T223000Z',
                startTime: '2023-12-13T15:30:00.000-07:00',
            },
        ],
    };
    const dummy_user4 = {
        email: 'dummy4@example.com',
        name: 'John Doe',
        address: '1650 E 12th Ave, Vancouver, BC',
        friends: ['friend1@example.com', 'friend2@example.com'],
    };

    const dummy_schedule5 = {
        email: 'dummy5@example.com',
        events: [
            {
                address: 'UBC Nest Building, Room 360',
                calendarID: 'koltonluu@gmail.com',
                endTime: '2023-12-03T17:00:00.000-07:00',
                eventName: 'Club Meeting',
                geolocation: { latitude: 0, longitude: 0 },
                id: '_64p36d1h6osjgchm6cp3gchk68r62oj3cgpj4d1m64tr_20231101T223000Z',
                startTime: '2023-12-03T15:30:00.000-07:00',
            },
            {
                address: 'UBC Nest Building, Room 360',
                calendarID: 'koltonluu@gmail.com',
                endTime: '2023-12-13T17:00:00.000-07:00',
                eventName: 'Club Meeting',
                geolocation: { latitude: 0, longitude: 0 },
                id: '_64p36d1h6osjgchm6cp3gchk68r62oj3cgpj4d1m64tr_20231101T223000Z',
                startTime: '2023-12-13T15:30:00.000-07:00',
            },
        ],
    };
    const dummy_user5 = {
        email: 'dummy5@example.com',
        name: 'John Doe',
        address: '2607 Nanaimo St, Vancouver, BC',
        friends: ['friend1@example.com', 'friend2@example.com'],
    };

    // Set up the test data before running the tests
    beforeAll(async () => {
        await client.db('ScheduleDB').collection('schedulelist').insertOne(dummy_schedule)
        await client.db('ScheduleDB').collection('schedulelist').insertOne(dummy_schedule2)
        await client.db('ScheduleDB').collection('schedulelist').insertOne(dummy_schedule4)
        await client.db('ScheduleDB').collection('schedulelist').insertOne(dummy_schedule5)
        await client.db('UserDB').collection('userlist').insertOne(dummy_user)
        await client.db('UserDB').collection('userlist').insertOne(dummy_user2)
        await client.db('UserDB').collection('userlist').insertOne(dummy_user3)
        await client.db('UserDB').collection('userlist').insertOne(dummy_user4)
        await client.db('UserDB').collection('userlist').insertOne(dummy_user5)
    });
    // Clean up the test data after running all the tests
    afterAll(async () => {
        await client.db('ScheduleDB').collection('schedulelist').deleteOne(dummy_schedule)
        await client.db('ScheduleDB').collection('schedulelist').deleteOne(dummy_schedule2)
        await client.db('ScheduleDB').collection('schedulelist').deleteOne(dummy_schedule4)
        await client.db('ScheduleDB').collection('schedulelist').deleteOne(dummy_schedule5)
        await client.db('UserDB').collection('userlist').deleteOne(dummy_user)
        await client.db('UserDB').collection('userlist').deleteOne(dummy_user2)
        await client.db('UserDB').collection('userlist').deleteOne(dummy_user3)
        await client.db('UserDB').collection('userlist').deleteOne(dummy_user4)
        await client.db('UserDB').collection('userlist').deleteOne(dummy_user5)
    });

    // Input: a user email and a friend email both exist in UserDB and have associated schedules in ScheduleDB, date parameter matches a date in both schedules
    // Expected status code: 200
    // Expected behavior: a commute route that both users can take together is returned in the form of an array
    // ChatGPT usage: Yes
    test('routes with friends, all inputs valid', async () => {
        const email = 'dummy@example.com';
        const friendEmail = 'dummy2@example.com';
        const date = '2023-12-03';
        const response = await request.get(`/api/recommendation/routesWithFriends/${email}/${friendEmail}/${date}`);
        
        expect(response.statusCode).toBe(200);
        expect(Array.isArray(response.body.routes)).toBe(true);
    });

    // Input: same as first test case, but friend and user email are swapped
    // Expected status code: 200
    // Expected behavior: a commute route that both users can take together is returned in the form of an array
    // ChatGPT usage: Yes
    test('routes with friends, all inputs valid', async () => {
        const email = 'dummy2@example.com';
        const friendEmail = 'dummy@example.com';
        const date = '2023-12-03';
        const response = await request.get(`/api/recommendation/routesWithFriends/${email}/${friendEmail}/${date}`);
        
        expect(response.statusCode).toBe(200);
        expect(Array.isArray(response.body.routes)).toBe(true);
    });

    // Input: same as first test case, but friend and user address are closer to Commercial Broadway
    // Expected status code: 200
    // Expected behavior: a commute route that both users can take together is returned in the form of an array
    // ChatGPT usage: Yes
    test('commercial broadway', async () => {
        const email = 'dummy4@example.com';
        const friendEmail = 'dummy5@example.com';
        const date = '2023-12-03';
        const response = await request.get(`/api/recommendation/routesWithFriends/${email}/${friendEmail}/${date}`);
        
        expect(response.statusCode).toBe(200);
        expect(Array.isArray(response.body.routes)).toBe(true);
    });

    // Input: Same as first test case, but friend email does not exist in UserDB
    // Expected status code: 400
    // Expected behavior: an error message is returned
    // ChatGPT usage: Yes
    test('friend email does not exist in database', async () => {
        const email = 'dummy@example.com';
        const friendEmail = 'dummy10@example.com';
        const date = '2023-12-03';
        const response = await request.get(`/api/recommendation/routesWithFriends/${email}/${friendEmail}/${date}`);
        
        expect(response.statusCode).toBe(400);
        expect(response.body).toEqual({message: 'No matching friend email exists in user database'});
    });

    // Input: Same as first test case, but user email does not exist in UserDB
    // Expected status code: 400
    // Expected behavior: an error message is returned
    // ChatGPT usage: Yes
    test('user email does not exist in database', async () => {
        const email = 'dummy100@example.com';
        const friendEmail = 'dummy2@example.com';
        const date = '2023-12-03';
        const response = await request.get(`/api/recommendation/routesWithFriends/${email}/${friendEmail}/${date}`);
        
        expect(response.statusCode).toBe(400);
        expect(response.body).toEqual({message: 'No matching user email exists in user database'});
    });

    // Input: Same as first test case, but user email does not have an associated schedule in ScheduleDB
    // Expected status code: 400
    // Expected behavior: an error message is returned
    // ChatGPT usage: Yes
    test('user schedule does not exist in database', async () => {
        const email = 'dummy3@example.com';
        const friendEmail = 'dummy2@example.com';
        const date = '2023-12-03';
        const response = await request.get(`/api/recommendation/routesWithFriends/${email}/${friendEmail}/${date}`);
        
        expect(response.statusCode).toBe(400);
        expect(response.body).toEqual({message: 'No matching user schedule exists in schedule database'});
    });

    // Input: Same as first test case, but friend email does not have an associated schedule in ScheduleDB
    // Expected status code: 400
    // Expected behavior: an error message is returned
    // ChatGPT usage: Yes
    test('friend schedule does not exist in database', async () => {
        const email = 'dummy@example.com';
        const friendEmail = 'dummy3@example.com';
        const date = '2023-12-03';
        const response = await request.get(`/api/recommendation/routesWithFriends/${email}/${friendEmail}/${date}`);
        
        expect(response.statusCode).toBe(400);
        expect(response.body).toEqual({message: 'No matching friend schedule exists in schedule database'});
    });  
    
    // Input: Same as first test case, but date parameter does not match the date of any event in user schedule
    // Expected status code: 400
    // Expected behavior: an error message is returned
    // ChatGPT usage: Yes
    test('bad date (user)', async () => {
        const email = 'dummy@example.com';
        const friendEmail = 'dummy2@example.com';
        const date = '2023-12-23';
        const response = await request.get(`/api/recommendation/routesWithFriends/${email}/${friendEmail}/${date}`);
        
        expect(response.statusCode).toBe(400);
        expect(response.body).toEqual({message: 'No matching date exists in user schedule'});
    });

    // Input: Same as first test case, but date parameter does not match the date of any event in friend schedule
    // Expected status code: 400
    // Expected behavior: an error message is returned
    // ChatGPT usage: Yes
    test('bad date (friend)', async () => {
        const email = 'dummy@example.com';
        const friendEmail = 'dummy2@example.com';
        const date = '2023-12-13';
        const response = await request.get(`/api/recommendation/routesWithFriends/${email}/${friendEmail}/${date}`);
        
        expect(response.statusCode).toBe(400);
        expect(response.body).toEqual({message: 'No matching date exists in friend schedule'});
    });
}); 

// Interface GET https://20.163.28.92:8081/api/recommendation/routes/:email/:date
describe('Create a transit route for a single user', () => {
    const dummy_schedule = {
        email: 'dummy@example.com',
        events: [
            {
                address: 'UBC Nest Building, Room 360',
                calendarID: 'koltonluu@gmail.com',
                endTime: '2023-12-03T17:00:00.000-07:00',
                eventName: 'Club Meeting',
                geolocation: { latitude: 0, longitude: 0 },
                id: '_64p36d1h6osjgchm6cp3gchk68r62oj3cgpj4d1m64tr_20231101T223000Z',
                startTime: '2023-12-03T15:30:00.000-07:00',
            },
            {
                address: 'UBC Nest Building, Room 360',
                calendarID: 'koltonluu@gmail.com',
                endTime: '2023-12-13T17:00:00.000-07:00',
                eventName: 'Club Meeting',
                geolocation: { latitude: 0, longitude: 0 },
                id: '_64p36d1h6osjgchm6cp3gchk68r62oj3cgpj4d1m64tr_20231101T223000Z',
                startTime: '2023-12-13T15:30:00.000-07:00',
            },
        ],
    };
    const dummy_user = {
        email: 'dummy@example.com',
        name: 'John Doe',
        address: '5870 Rumble Street, Burnaby, BC',
        friends: ['friend1@example.com', 'friend2@example.com'],
    };

    const dummy_user2 = {
        email: 'dummy2@example.com', // fake email, wont work with google authentication
        name: 'Leon Guo',
        address: '7746 Berkley Street, Burnaby, BC',
        friends: ['friend1@example.com', 'friend2@example.com'],
    };

    // Set up the test data before running the tests
    beforeAll(async () => {
        await client.db('ScheduleDB').collection('schedulelist').insertOne(dummy_schedule)
        await client.db('UserDB').collection('userlist').insertOne(dummy_user)
        await client.db('UserDB').collection('userlist').insertOne(dummy_user2)
    });
    // Clean up the test data after running all the tests
    afterAll(async () => {
        await client.db('ScheduleDB').collection('schedulelist').deleteOne(dummy_schedule)
        await client.db('UserDB').collection('userlist').deleteOne(dummy_user)
        await client.db('UserDB').collection('userlist').deleteOne(dummy_user2)
    });

    // Input: a user email exists in UserDB and has an associated schedule in ScheduleDB, date parameter matches a date in the user's schedule
    // Expected status code: 200
    // Expected behavior: a commute route is returned in the form of an array
    // ChatGPT usage: Yes
    test('initialize route with all valid inputs', async () => {
        const email = 'dummy@example.com';
        const date = '2023-12-03';
        const response = await request.get(`/api/recommendation/routes/${email}/${date}`);
        
        expect(response.statusCode).toBe(200);
        expect(Array.isArray(response.body.routes)).toBe(true);
    });

    // Input: An email that doesn't exist in UserDB, any date
    // Expected status code: 400
    // Expected behavior: an error message is returned
    // ChatGPT usage: Yes
    test('user email does not exist in database', async () => {
        const email = 'dummy17@example.com';
        const date = '2023-12-03';
        const response = await request.get(`/api/recommendation/routes/${email}/${date}`);
        // console.log("!!!!!!!!" + response.body.message);
        expect(response.statusCode).toBe(400);
        expect(response.body).toEqual({message: 'No matching email exists in user database'});
    });

    // Input: An email that exists in UserDB but has no associated schedule in ScheduleDB, any date
    // Expected status code: 400
    // Expected behavior: an error message is returned
    // ChatGPT usage: Yes
    test('user schedule does not exist in database', async () => {
        const email = 'dummy2@example.com';
        const date = '2023-12-03';
        const response = await request.get(`/api/recommendation/routes/${email}/${date}`);
        // console.log("*******" + response.body.message);
        expect(response.statusCode).toBe(400);
        expect(response.body).toEqual({message: 'No matching schedule exists in schedule database'});
    });

    // Input: An email that exists in UserDB and has an associated schedule in ScheduleDB, but the date parameter does not match any date in the user's schedule
    // Expected status code: 400
    // Expected behavior: an error message is returned
    // ChatGPT usage: Yes
    test('bad date', async () => {
        const email = 'dummy@example.com';
        const date = '2023-12-33';
        const response = await request.get(`/api/recommendation/routes/${email}/${date}`);
        
        expect(response.statusCode).toBe(400);
        expect(response.body).toEqual({message: 'No matching date exists in user schedule'});
    });
});

