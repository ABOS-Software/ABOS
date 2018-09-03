import React from 'react';

import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import Drawer from '@material-ui/core/Drawer';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import List from '@material-ui/core/List';
import Typography from '@material-ui/core/Typography';
import IconButton from '@material-ui/core/IconButton';
import Hidden from '@material-ui/core/Hidden';
import Divider from '@material-ui/core/Divider';
import MenuIcon from '@material-ui/icons/Menu';
import AddIcon from '@material-ui/icons/Add';
import MoreVert from '@material-ui/icons/MoreVert';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import Modal from '@material-ui/core/Modal';
import classNames from 'classnames';
import Menu from '@material-ui/core/Menu';
import MenuItem from '@material-ui/core/MenuItem';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import update from 'immutability-helper';
import SaveIcon from '@material-ui/icons/Save';
import Button from '@material-ui/core/Button';
import {push} from 'react-router-redux';
import {connect} from 'react-redux';
import DialogTitle from '@material-ui/core/DialogTitle';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import Select from '@material-ui/core/Select';
import InputLabel from '@material-ui/core/InputLabel';
import FormControl from '@material-ui/core/FormControl';
import TextField from '@material-ui/core/TextField';
import ProductsGrid from './ProductsGrid.js';
import {
    BooleanInput,
    CREATE,
    fetchUtils,
    FormDataConsumer,
    GET_LIST,
    ImageField,
    ImageInput,
    ReferenceArrayInput,
    ReferenceInput,
    required,
    SelectArrayInput,
    SelectInput,
    SimpleForm,
    TextInput
} from 'react-admin';
import Paper from '@material-ui/core/Paper';

import restClient, {GET_PLAIN_MANY} from "../grailsRestClient";
import UserPanel from "./UserPanel";


const drawerWidth = 240;


const httpClient = (url, options = {}) => {
    if (!options.headers) {
        options.headers = new Headers({Accept: 'application/json'});
    }
    const token = localStorage.getItem('access_token');
    options.headers.set('Authorization', `Bearer ${token}`);
    return fetchUtils.fetchJson(url, options);
};
const dataProvider = restClient(httpClient);

function TabContainer(props) {
    return (
        <Typography component="div" {...props}>
            {props.children}
        </Typography>
    );
}

TabContainer.propTypes = {
    children: PropTypes.node.isRequired,
};


const styles = theme => ({
    root: {
        flexGrow: 1,
        zIndex: 1,
        overflow: 'hidden',
        position: 'relative',
        display: 'flex',
        width: '100%',
        height: '100%'
    },
    heading: {
        fontSize: theme.typography.pxToRem(15),
    },
    userPanel: {
        width: '100%',
    },
    modal: {
        top: '10%',
        left: '10%',
        width: '80%',
        height: '80%',
        position: 'absolute',
    },
    appBar: {
        position: 'absolute',

        zIndex: theme.zIndex.drawer + 1,

    },
    navIconHide: {
        [theme.breakpoints.up('md')]: {
            display: 'none',
        },
    },
    toolbar: theme.mixins.toolbar,
    drawerPaper: {
        width: drawerWidth,
        [theme.breakpoints.up('md')]: {
            position: 'relative',
        },
    },
    content: {
        display: 'flex',
        flexDirection: 'column',
        flexGrow: 1,
        backgroundColor: theme.palette.background.default,
        padding: theme.spacing.unit,
        transition: theme.transitions.create('margin', {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.leavingScreen,
        }),
    },
    'content-left': {
        marginLeft: -drawerWidth,
    },
    'content-right': {
        marginRight: -drawerWidth,
    },
    contentShift: {
        transition: theme.transitions.create('margin', {
            easing: theme.transitions.easing.easeOut,
            duration: theme.transitions.duration.enteringScreen,
        }),
    },
    'contentShift-left': {
        marginLeft: 0,
    },
    'contentShift-right': {
        marginRight: 0,
    },
    flex: {
        flexGrow: 1,
    },
    topLevelExpansionPanel: {
        display: 'block',
    },
    'tabScroll': {
        height: '85%',
        overflow: 'scroll',
    },
    'tabNoScroll': {
        height: '85%'
    },
    fullHeight: {
        height: '100%',
    },

    leftIcon: {
        marginRight: theme.spacing.unit,
    },
    rightIcon: {
        marginLeft: theme.spacing.unit,
    },
    iconSmall: {
        fontSize: 20,
    },
    button: {
        margin: theme.spacing.unit,
    },
    bottomBar: {
        position: 'absolute',
        bottom: 10,
        right: 10
    },
    productsGrid: {
        height: '100% !important',
        display: 'flex',
        flexDirection: 'column'
    }
});



class UGYEditor extends React.Component {
    //users: {}, years: {}, customers: {}
    //users: {}, years: {}, customers: {}
    state = {
        tab: 0,
        yearNavOpen: true,
        anchor: 'left',
        update: false,
        userBulkMenuAnchor: null,
        userAddMenuAnchor: null,
        ready: false,
        userChecks: [],
        years: [],
        groups: [],
        open: true,
        selectedGroup: 0,
        addUsersToGroupOpen: false,
        selectedUser: "",
        addUsersToUserOpen: false,
        addUserOpen: false,
        addUser: {
            userName: "",
            password: "",
            fullName: "",
        },
        importExportOpen: false,

    };




    constructor(props) {
        super(props);

    }



    getYears() {
        dataProvider(GET_LIST, 'Years', {
            filter: {},
            sort: {field: 'id', order: 'DESC'},
            pagination: {page: 1, perPage: 1000},
        }).then(response => {
            this.setState({years: response.data})
        })


    }

    getGroups() {
        dataProvider(GET_LIST, 'Group', {
            filter: {},
            sort: {field: 'id', order: 'DESC'},
            pagination: {page: 1, perPage: 1000},
        }).then(response => {
            this.setState({groups: response.data})
        })


    }

    getUsers() {
        /*     dataProvider(GET_LIST, 'User', {
                 filter: {},
                 sort: {field: 'id', order: 'DESC'},
                 pagination: {page: 1, perPage: 1000},
             }).then(response => {
                 let users = response.data;
                 let userChecks = {};
                 users.forEach(user => {
                     let userName = user.userName;
                     let userState = {};
                     users.forEach(subUser => {
                         userState[subUser.userName] = false;
                     });

                     userChecks[userName] = {checked: false, groups: -1, subUsers: userState};
                 });
                 this.setState({'users': users, 'update': true, 'userChecks': userChecks})
             });*/
        dataProvider(GET_PLAIN_MANY, 'UserHierarchy', {}).then(response => {
            let users = response.data;
            let userChecks = {};
            Object.keys(users).forEach(user => {

                userChecks[user] = {checked: false, group: users[user].group, subUsers: users[user].subUsers};
            });
            this.setState({'users': users, 'update': true, 'userChecks': userChecks})
        });


    }


    handleDrawerToggle = () => {
        this.setState(state => ({yearNavOpen: !state.yearNavOpen}));
    };

    handleCheckBoxChange = name => event => {
        let parentState = update(this.state.userChecks, {
            [name]: {checked: {$set: event.target.checked}}
        });

        this.setState({userChecks: parentState, update: true});
        //this.setState({[name]: event.target.checked});
    };

    handleGroupChange = name => event => {
        let parentState = update(this.state.userChecks, {
            [name]: {group: {$set: event.target.value}}
        });

        this.setState({userChecks: parentState, update: true});
        //this.setState({[name]: event.target.checked});
    };

    handleManageCheckBoxChange = (parent, name) => event => {

        let parentState = update(this.state.userChecks, {
            [parent]: {subUsers: {[name]: {checked: {$set: event.target.checked}}}}
        });

        this.setState({userChecks: parentState, update: true});


    };

    handleChangeAnchor = event => {
        this.setState({
            anchor: event.target.value,
        });
    };

    handleUserBulkMenu = event => {
        this.setState({userBulkMenuAnchor: event.currentTarget});
    };

    handleUserBulkMenuClose = () => {
        this.setState({userBulkMenuAnchor: null});
    };

    handleUserAddMenu = event => {
        this.setState({userAddMenuAnchor: event.currentTarget});
    };

    handleUserAddMenuClose = () => {
        this.setState({userAddMenuAnchor: null});
    };

    handleChange = (event, value) => {
        this.setState({value});
    };
    handleTabChange = (event, value) => {
        this.setState({tab: value});
    };


    save = event => {
        console.log(this.state.userChecks);
                let options = {};
        let url = 'http://localhost:8080/api/UserHierarchy';
                if (!options.headers) {
                    options.headers = new Headers({Accept: 'application/json'});
                }
                const token = localStorage.getItem('access_token');
                options.headers.set('Authorization', `Bearer ${token}`);

                fetch(url, {
                    method: "POST",
                    mode: "cors",
                    cache: "no-cache",
                    credentials: "same-origin", // include, same-origin, *omit
                    headers: {
                        "Content-Type": "application/json; charset=utf-8",
                        'Authorization': `Bearer ${token}`
                        // "Content-Type": "application/x-www-form-urlencoded",
                    },
                    redirect: "follow", // manual, *follow, error
                    referrer: "no-referrer", // no-referrer, *client
                    body: JSON.stringify(this.state.userChecks),
                }).then(response => {
                    //  this.setState({open: false});
                    //  this.props.push('/');
                });


        //console.log(fetchUtils.fetchJson(url, options));


    };

    cancel = event => {
        this.setState({open: false});
        this.props.push('/');


    };

    addSelectedUsersToGroup = event => {
        let parentState = this.state.userChecks;


        Object.keys(this.state.userChecks).filter(userName => this.state.userChecks[userName].checked).forEach(userName => {
            parentState = update(parentState, {
                [userName]: {group: {$set: this.state.selectedGroup}}
            });
        });
        this.setState({userChecks: parentState, addUsersToGroupOpen: false});

    };

    addSelectedUsersToGroupClicked = event => {

        this.setState({addUsersToGroupOpen: true});
        this.handleUserBulkMenuClose(event);
    };


    addSelectedUsersToUser = event => {
        let parentState = this.state.userChecks;


        Object.keys(this.state.userChecks[this.state.selectedUser].subUsers).filter(userName => this.state.userChecks[userName].checked).forEach(userName => {
            parentState = update(parentState, {
                [this.state.selectedUser]: {subUsers: {[userName]: {checked: {$set: true}}}}
            });
        });
        this.setState({userChecks: parentState, addUsersToUserOpen: false});
    };
    addSelectedUsersToUserClicked = event => {

        this.setState({addUsersToUserOpen: true});
        this.handleUserBulkMenuClose(event);
    };

    addSingleUser = event => {
        dataProvider(CREATE, 'User', {
            data: {
                username: this.state.addUser.userName,
                password: this.state.addUser.password
            }
        }).then(response => {
            this.setState({addUserOpen: false});

        });
    };
    addSingleUserClick = event => {
        this.setState({addUserOpen: true});

        this.handleUserAddMenuClose(event);
    };
    handleImportExportClick = event => {
        this.setState({importExportOpen: true});

        this.handleUserAddMenuClose(event);
    };

    addBulkUser = event => {

        this.handleUserAddMenuClose(event);
    };
    addBulkUserClick = event => {

        this.handleUserAddMenuClose(event);
    };

    /*
    Feature not yet implemented
     */
    enableSelectedUsers = event => {
        Object.keys(this.state.userChecks).filter(userName => this.state.userChecks[userName].checked).forEach(userName => {

        });
        this.handleUserBulkMenuClose(event);
    };

    archiveSelectedUsers = event => {
        Object.keys(this.state.userChecks).filter(userName => this.state.userChecks[userName].checked).forEach(userName => {

        });
        this.handleUserBulkMenuClose(event);
    };

    removeSelectedUsersFromYear = event => {
        Object.keys(this.state.userChecks).filter(userName => this.state.userChecks[userName].checked).forEach(userName => {

        });
        this.handleUserBulkMenuClose(event);
    };





    renderEnabledUsers = () => {
        const {classes, theme} = this.props;
        let users = ['me', 'test1'];
        let userPanels = [];
        Object.keys(this.state.userChecks).forEach(user => {
            let userName = user;
            userPanels.push(<UserPanel key={userName} userName={userName} userChecks={this.state.userChecks}
                                       handleManageCheckBoxChange={this.handleManageCheckBoxChange}
                                       handleCheckBoxChange={this.handleCheckBoxChange}
                                       checked={this.state.userChecks[userName].checked}
                                       handleGroupChange={this.handleGroupChange}
                                       group={this.state.userChecks[userName].group}
                                       groups={this.state.groups}/>)
        });
        return (
            <ExpansionPanel className={classes.topLevelExpansionPanel}>
                <ExpansionPanelSummary expandIcon={<ExpandMoreIcon/>}>
                    <div className={classes.flex}>

                        <Typography className={classes.heading}>Enabled Users</Typography>
                    </div>

                </ExpansionPanelSummary>
                <ExpansionPanelDetails className={classes.topLevelExpansionPanel}>
                    {
                        userPanels
                    }

                </ExpansionPanelDetails>
            </ExpansionPanel>
        )
    };

    renderGroupItems = () => {
        let groupList = [];
        this.state.groups.forEach(group => {
            groupList.push(<MenuItem key={"AddGroupToUser-group-" + group.id}
                                     value={group.id}>{group.groupName}</MenuItem>);

        });
        return groupList;
    };

    renderUserItems = () => {
        let userList = [];
        Object.keys(this.state.userChecks).forEach(userName => {
            userList.push(<MenuItem key={"AddUserToUser-user-" + userName} value={userName}>{userName}</MenuItem>);

        });
        return userList;
    };
    updateAddUserState = (field, value) => {
        let parentState = update(this.state.addUser, {
            [field]: {$set: value}
        });
        this.setState({addUser: parentState})
    };
    render() {
        const {classes, theme} = this.props;
        if (this.state.ready) {

            const {tab, anchor, yearNavOpen, userBulkMenuAnchor, userAddMenuAnchor} = this.state;
            const userBulkMenuOpen = Boolean(userBulkMenuAnchor);
            const userAddMenuOpen = Boolean(userAddMenuAnchor);
            const dialogs = [
                <Dialog
                    key={"addUsersToGroupDialog"}
                    open={this.state.addUsersToGroupOpen}
                    onClose={event => this.setState({addUsersToGroupOpen: false})}
                    aria-labelledby="form-dialog-title"
                >
                    <DialogTitle id="form-dialog-title">Add Selected Users to Group</DialogTitle>
                    <DialogContent>
                        <DialogContentText>
                            Please select the group to add the users to
                        </DialogContentText>
                        <FormControl className={classes.formControl}>
                            <InputLabel htmlFor="addUsersToGroup-GroupSelection">Group</InputLabel>
                            <Select
                                value={this.state.selectedGroup}
                                onChange={event => {
                                    this.setState({selectedGroup: event.target.value})
                                }}
                                inputProps={{
                                    name: 'GroupSelection',
                                    id: 'addUsersToGroup-GroupSelection',
                                }}
                            >
                                {this.renderGroupItems()

                                }
                            </Select>
                        </FormControl>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={event => this.setState({addUsersToGroupOpen: false})} color="primary">
                            Cancel
                        </Button>
                        <Button onClick={this.addSelectedUsersToGroup} color="primary">
                            Apply
                        </Button>
                    </DialogActions>
                </Dialog>,
                <Dialog
                    key={"addUsersToUserDialog"}

                    open={this.state.addUsersToUserOpen}
                    onClose={event => this.setState({addUsersToUserOpen: false})}
                    aria-labelledby="form-dialog-title"
                >
                    <DialogTitle id="form-dialog-title">Add Selected Users to User</DialogTitle>
                    <DialogContent>
                        <DialogContentText>
                            Please select the user to add the users to
                        </DialogContentText>
                        <FormControl className={classes.formControl}>
                            <InputLabel htmlFor="addUsersToGroup-GroupSelection">User</InputLabel>
                            <Select
                                value={this.state.selectedUser}
                                onChange={event => {
                                    this.setState({selectedUser: event.target.value})
                                }}
                                inputProps={{
                                    name: 'UserSelection',
                                    id: 'addUsersToUser-UserSelection',
                                }}
                            >
                                {this.renderUserItems()

                                }
                            </Select>
                        </FormControl>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={event => this.setState({addUsersToUserOpen: false})} color="primary">
                            Cancel
                        </Button>
                        <Button onClick={this.addSelectedUsersToUser} color="primary">
                            Apply
                        </Button>
                    </DialogActions>
                </Dialog>,
                <Dialog
                    key={"addUserDialog"}

                    open={this.state.addUserOpen}
                    onClose={event => this.setState({addUserOpen: false})}
                    aria-labelledby="form-dialog-title"
                >
                    <DialogTitle id="form-dialog-title">Add User</DialogTitle>
                    <DialogContent>
                        <DialogContentText>
                            Please enter the information about the user
                        </DialogContentText>
                        <FormControl className={classes.formControl}>
                            <InputLabel htmlFor="AddUser-Username">Username</InputLabel>
                            <TextField
                                value={this.state.addUser.userName}
                                onChange={event => {
                                    this.updateAddUserState("userName", event.target.value)
                                }}
                                inputProps={{
                                    name: 'UserName',
                                    id: 'AddUser-Username',
                                }}
                            >

                            </TextField>
                        </FormControl>
                        <FormControl className={classes.formControl}>
                            <InputLabel htmlFor="AddUser-Password">Password</InputLabel>
                            <TextField
                                value={this.state.addUser.password}
                                type="password"
                                onChange={event => {
                                    this.updateAddUserState("password", event.target.value)
                                }}

                                inputProps={{
                                    name: 'Password',
                                    id: 'AddUser-Password',
                                }}
                            >

                            </TextField>
                        </FormControl>
                        <FormControl className={classes.formControl}>
                            <InputLabel htmlFor="AddUser-FullName">FullName</InputLabel>
                            <TextField
                                value={this.state.addUser.fullName}
                                onChange={event => {
                                    this.updateAddUserState("fullName", event.target.value)
                                }}

                                inputProps={{
                                    name: 'FullName',
                                    id: 'AddUser-FullName',
                                }}
                            >

                            </TextField>
                        </FormControl>

                    </DialogContent>
                    <DialogActions>
                        <Button onClick={event => this.setState({addUserOpen: false})} color="primary">
                            Cancel
                        </Button>
                        <Button onClick={this.addSingleUser} color="primary">
                            Apply
                        </Button>
                    </DialogActions>
                </Dialog>
            ];
            const drawer = (
                <div>
                    <div className={classes.toolbar}/>
                    <Divider/>
                    <List>
                        <ListItem button>
                            <ListItemText primary="Trash"/>
                        </ListItem>
                    </List>

                </div>
            );
            const usersTab = (
                <div>

                    <Toolbar>
                        <Typography variant="title" color="inherit" className={classes.flex}>

                        </Typography>
                        <div>
                            <IconButton
                                aria-owns={userAddMenuOpen ? 'user-add-menu' : null}
                                aria-haspopup="true"
                                onClick={this.handleUserAddMenu}
                                color="inherit"
                            >
                                <AddIcon/>
                            </IconButton>
                            <Menu
                                id="user-add-menu"
                                anchorEl={userAddMenuAnchor}
                                anchorOrigin={{
                                    vertical: 'top',
                                    horizontal: 'right',
                                }}
                                transformOrigin={{
                                    vertical: 'top',
                                    horizontal: 'right',
                                }}
                                open={userAddMenuOpen}
                                onClose={this.handleUserAddMenuClose}
                            >
                                <MenuItem onClick={this.addSingleUserClick}>Add Single User</MenuItem>
                                <MenuItem onClick={this.addBulkUserClick}>Add Bulk Users</MenuItem>
                            </Menu>
                            <IconButton
                                aria-owns={userBulkMenuOpen ? 'user-Bulk-Menu' : null}
                                aria-haspopup="true"
                                onClick={this.handleUserBulkMenu}
                                color="inherit"
                            >
                                <MoreVert/>
                            </IconButton>
                            <Menu
                                id="user-Bulk-Menu"
                                anchorEl={userBulkMenuAnchor}
                                anchorOrigin={{
                                    vertical: 'top',
                                    horizontal: 'right',
                                }}
                                transformOrigin={{
                                    vertical: 'top',
                                    horizontal: 'right',
                                }}
                                open={userBulkMenuOpen}
                                onClose={this.handleUserBulkMenuClose}
                            >
                                <MenuItem onClick={this.addSelectedUsersToGroupClicked}>Add Selected to Group</MenuItem>
                                <MenuItem onClick={this.removeSelectedUsersFromYear}>Remove Selected from
                                    year</MenuItem>
                                <MenuItem onClick={this.enableSelectedUsers}>Enable Selected</MenuItem>
                                <MenuItem onClick={this.addSelectedUsersToUserClicked}>Add selected to User</MenuItem>
                                <MenuItem onClick={this.archiveSelectedUsers}>Archive selected users</MenuItem>
                            </Menu>

                        </div>

                    </Toolbar>
                    <div>
                        {this.renderEnabledUsers()}
                    </div>
                </div>
            );
            const groupsTab = (
                <div>
                    Groups

                </div>
            );
            const prodsTab = (
                <div className={classes.productsGrid}>
                    Products
                    <Paper className={classes.fullHeight}>
                        <ProductsGrid year={5} onImportExport={this.handleImportExportClick}/>
                    </Paper>
                </div>
            );
            /*
            *                         | Tab Pane
            *                         |    Users | Groups | Products
            *                         |
            *                         |     U Menu Bar - Add Element (Dropdown for bulk or simple) Multi Action Menu
            *                         |     S   Expansion Panels( Enabled, Disabled, Archived)
            *                         |     E     Selectable Expansion Panels with Delete/Edit buttons on end
            *                         |     R       Group Selection
            *                         |     S       Management Selection - Use Selectable Nested List
            *                         |
            *      Nested List        |     G Menu Bar - Add Element (Dropdown for bulk or simple) Multi Action Menu
            * See List on Material UI |     R   Expansion Panels with Delete/Edit buttons on end E
            *                         |     O     Edit Button Has option to remove all selected groups members from groups
            *                         |     U   List of Group Members(Selectable)
            *                         |     P
            *                         |
            *                         |     P Mimic Add Customer, but Some Changes
            *                         |     R   Top Pane
            *                         |     O     Button for import/export
            *                         |     D   Add Product inputs/Button
            *                         |     U   Table - implement row selection Toolbar(see all features, use AdvancedToolbar with custom stuffs)
            *                         |     C   No Quantity/Extended Cost
            *                         |     T   Add Category Selection
            *                         |     S     Include Add Category Button - Should open a modal dialog
            *                         |
            *                         |------------------------------------------------------------------- Save | Cancel ---
             */
            return (
                <div>
                    <Modal

                        open={this.state.open}
                        disableBackdropClick={true}
                    >
                        <div className={classes.modal}>
                            <div className={classes.root}>
                                <AppBar className={classes.appBar}>
                                    <Toolbar>
                                        <IconButton
                                            color="inherit"
                                            aria-label="Open drawer"
                                            onClick={this.handleDrawerToggle}
                                        >
                                            <MenuIcon/>
                                        </IconButton>
                                        <Typography variant="title" color="inherit" noWrap>
                                            Users, Groups, and Years
                                        </Typography>
                                    </Toolbar>
                                </AppBar>
                                <Hidden mdUp>

                                    <Drawer
                                        variant="permanent"
                                        open={this.state.yearNavOpen}
                                        onClose={this.handleDrawerToggle}
                                        classes={{
                                            paper: classes.drawerPaper,
                                        }}
                                    >
                                        {drawer}
                                    </Drawer>
                                </Hidden>
                                <Hidden smDown implementation="css" className={classes.fullHeight}>
                                    <Drawer
                                        variant="persistent"
                                        anchor={theme.direction === 'rtl' ? 'right' : 'left'}

                                        open={this.state.yearNavOpen}
                                        onClose={this.handleDrawerToggle}
                                        classes={{
                                            paper: classes.drawerPaper,
                                        }}
                                        className={classes.fullHeight}
                                    >
                                        {drawer}
                                    </Drawer>
                                </Hidden>
                                <main className={classNames(classes.content, classes[`content-${anchor}`], {
                                    [classes.contentShift]: yearNavOpen,
                                    [classes[`contentShift-${anchor}`]]: yearNavOpen,
                                })}>
                                    <div className={classes.toolbar}/>

                                    <Tabs value={tab} onChange={this.handleTabChange}>
                                        <Tab label="Users"/>
                                        <Tab label="Groups"/>
                                        <Tab label="Products"/>
                                    </Tabs>
                                    {tab === 0 && <TabContainer className={classes.tabScroll}>{usersTab}</TabContainer>}
                                    {tab === 1 &&
                                    <TabContainer className={classes.tabScroll}>{groupsTab}</TabContainer>}
                                    {tab === 2 &&
                                    <TabContainer className={classes.tabNoScroll}>{prodsTab}</TabContainer>}
                                    <Toolbar>
                                        <div className={classes.bottomBar}>
                                            <Button variant="contained" color="secondary" className={classes.button}
                                                    onClick={this.cancel}>
                                                Cancel
                                            </Button>
                                            <Button variant="contained" color="primary" className={classes.button}
                                                    onClick={this.save}>
                                                <SaveIcon className={classNames(classes.leftIcon, classes.iconSmall)}/>
                                                Save
                                            </Button>
                                        </div>
                                    </Toolbar>
                                </main>
                            </div>
                        </div>

                    </Modal>
                    {dialogs}

                </div>

            )
        } else {
            return (<h2>Loading...</h2>)
        }
    }


    componentWillMount() {

    }

    componentWillReceiveProps() {
        this.getUsers();
        this.getYears();
        this.getGroups();
        this.setState({ready: true})
    }

    shouldComponentUpdate() {
        if (this.state.groups.length > 0 && Object.keys(this.state.userChecks).length > 0) {
            return true
        }
        /*        if (this.state.update === true) {
                    this.setState({'update': false});
                    return true
                }*/
        return false
    }
}

UGYEditor.propTypes = {
    push: PropTypes.func,
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
};
export default connect(null, {
    push,
})(withStyles(styles, {withTheme: true})(UGYEditor));

