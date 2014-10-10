/**
 * Represents the application api.  If the variable is already defined use it,
 * otherwise create an empty object.
 *
 * @type {EScreeningDashboardApp|*|EScreeningDashboardApp|*|{}|{}}
 */
var EScreeningDashboardApp = EScreeningDashboardApp || {};
/**
 * Represents the application static variable. Use existing static variable, if one already exists,
 * otherwise create a new one.
 *
 * @static
 * @type {*|EScreeningDashboardApp.models|*|EScreeningDashboardApp.models|Object|*|Object|*}
 */
EScreeningDashboardApp.models = EScreeningDashboardApp.models || EScreeningDashboardApp.namespace("gov.va.escreening.models");
/**
 * Constructor method for the TemplateType class.  The properties of this class can be initialized with
 * the jsonTemplateObject.
 * @class
 * @classdesc   This class is a domain model class; which means it has both behavior and state
 *              information about Template Types.
 * @param {String}  jsonTemplateObject  Represents the JSON representation of a TemplateType object.
 * @constructor
 * @author Robin Carnow
 */
EScreeningDashboardApp.models.TemplateType = function (jsonTemplateObject) {
    var that = this,
        id = (Object.isDefined(jsonTemplateObject) && Object.isDefined(jsonTemplateObject.id))? jsonTemplateObject.id : -1,
        name = (Object.isDefined(jsonTemplateObject) && Object.isDefined(jsonTemplateObject.name))? jsonTemplateObject.name : null,
        description = (Object.isDefined(jsonTemplateObject) && Object.isDefined(jsonTemplateObject.description))? jsonTemplateObject.description : null,
        templateId = (Object.isDefined(jsonTemplateObject) && Object.isDefined(jsonTemplateObject.templateId)) ? jsonTemplateObject.templateId : null,
        exists =  Object.isDefined(templateId);

    /*this.getId = function(){
        return id;
    };

    this.getName = function() {
        return name;
    };

    this.getDescription = function() {
        return description;
    };

    this.getExists = function() {
        return exists;
    };*/

    this.toString = function () {
        return "TemplateType {id: " + this.id + ", name: " + this.name + ", description: " + this.description + ", exists: " + this.exists + "}";
    };

    /*this.toJSON = function () {
        return JSON.stringify({ 
            id: (Object.isDefined(id) && id > 0)? id : null,
            name: name,
            description: description,
            "templateId" : templateId
        });
    };
    
    this.toUIObject = function(){
        return {
            "id" : id,
            "name" : name,
            "description" : description,
            "templateId" : templateId,
            "exists" : exists
        }
    };*/
};
/*
EScreeningDashboardApp.models.TemplateType.toUIObjects = function(templateTypes) {
    var templateTypesUIObjects = [];

    if(Object.isDefined(templateTypes) && Object.isArray(templateTypes)) {
        templateTypes.forEach(function(templateType) {
            templateTypesUIObjects.push(templateType.toUIObject());
        });
    }

    return templateTypesUIObjects;
};

*/
/**
 * Static method to translated an array of ModuleTemplateTypeDTO (in JSON) into an array of TemplateType domain model objects.
 *
 * @static
 * @method
 * @param {String}  jsonResponse  Represents the JSON response of a service call request.
 * @returns {EScreeningDashboardApp.models.TemplateType[]} A list of TemplateTypes that have been returned from a service call request.
 *
 * @author Robin Carnow
 *//*

EScreeningDashboardApp.models.TemplateType.transformJSONPayload = function (jsonResponse) {
    'use strict';
    */
/**
     * Represent list of template types.
     *
     * @private
     * @field
     * @type {EScreeningDashboardApp.models.TemplateType[]}
     *//*

    var templateTypes = [];

    if (!Object.isDefined(jsonResponse)) {
        throw new BytePushers.exceptions.InvalidParameterException("Server response can not be null or undefined.");
    }

    if (!Array.isArray(jsonResponse)) {
        throw new BytePushers.exceptions.InvalidParameterException("Server response must be an Array.");
    }

    jsonResponse.forEach(function(jsonTemplateType){
        templateTypes.push(new EScreeningDashboardApp.models.TemplateType(jsonTemplateType));
    });


    return templateTypes;
};*/
