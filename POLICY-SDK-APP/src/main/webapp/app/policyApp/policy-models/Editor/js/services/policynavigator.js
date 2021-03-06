/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Engine
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
(function(angular) {
    'use strict';
    angular.module('abs').service('policyNavigator', [
        '$http', '$q', 'policyManagerConfig', 'item', function ($http, $q, policyManagerConfig, Item) {

        $http.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';

        var PolicyNavigator = function() {
            this.requesting = false;
            this.fileList = [];
            this.currentPath = [];
            this.history = [];
            this.error = '';
            this.searchModalActive = false;
        };

        PolicyNavigator.prototype.setSearchModalActiveStatus = function(){
        	this.searchModalActive = true;
        };
        
        PolicyNavigator.prototype.deferredHandler = function(data, deferred, defaultMsg) {
            if (!data || typeof data !== 'object') {
                this.error = 'Bridge response error, please check the docs';
            }
            if (!this.error && data.result && data.result.error) {
                this.error = data.result.error;
            }
            if (!this.error && data.error) {
                this.error = data.error.message;
            }
            if (!this.error && defaultMsg) {
                this.error = defaultMsg;
            }
            if (this.error) {
                return deferred.reject(data);
            }
            return deferred.resolve(data);
        };
        
        PolicyNavigator.prototype.deferredSearchHandler = function(data, deferred, defaultMsg) {
        	self.fileList = [];
        	 self.error = '';
            if (!data || typeof data !== 'object') {
                this.error = 'Bridge response error, please check the docs';
            }
            if (!this.error && data.result && data.result.error) {
                this.error = data.result.error;
            }
            if (!this.error && data.error) {
                this.error = data.error.message;
            }
            if (!this.error && defaultMsg) {
                this.error = defaultMsg;
            }
            if (this.error) {
                return deferred.reject(data);
            }
            return deferred.resolve(data);
        };

        PolicyNavigator.prototype.list = function() {
            var self = this;
            var deferred = $q.defer();
            var path = self.currentPath.join('/');
            var data = {params: {
                mode: 'LIST',
                onlyFolders: false,
                path: '/' + path
            }};

            self.requesting = true;
            self.fileList = [];
            self.error = '';

            $http.post(policyManagerConfig.listUrl, data).success(function(data) {
                self.deferredHandler(data, deferred);
            }).error(function(data) {
                self.deferredHandler(data, deferred, 'Unknown error listing, check the response');
            })['finally'](function() {
                self.requesting = false;
            });
            return deferred.promise;
        };

        PolicyNavigator.prototype.refresh = function() {
            var self = this;
            var path = self.currentPath.join('/');
            if(self.searchModalActive){
            	return self.searchlist(null).then(function(data) {
            		self.fileList = (data.result || []).map(function(file) {
            			return new Item(file, self.currentPath);
            		});
            		self.buildTree(path);
            	});	
            }else{
            	return self.list().then(function(data) {
            		self.fileList = (data.result || []).map(function(file) {
            			return new Item(file, self.currentPath);
            		});
            		self.buildTree(path);
            	});
            }
        };
        
        PolicyNavigator.prototype.searchlist = function(policyList) {
            var self = this;
            var deferred = $q.defer();
            var path = self.currentPath.join('/');
            var data;
            if(policyList == null){
            	 data = {params: {
                     mode: 'SEARCHLIST',
                     onlyFolders: false,
                     path: '/' + path
                 }};
            }else{
            	 data = {params: {
                     mode: 'SEARCHLIST',
                     onlyFolders: false,
                     path: '/' + path,
                     policyList : policyList
                 }};
            }
           

            self.requesting = true;
            self.fileList = [];
            self.error = '';

            $http.post(policyManagerConfig.searchListUrl, data).success(function(data) {
                self.deferredHandler(data, deferred);
            }).error(function(data) {
                self.deferredHandler(data, deferred, 'Unknown error listing, check the response');
            })['finally'](function() {
                self.requesting = false;
            });
            return deferred.promise;
        };

        PolicyNavigator.prototype.searchrefresh = function(policyList) {
        	var self = this;
        	var path = self.currentPath.join('/');
        	return self.searchlist(policyList).then(function(data) {
        		self.fileList = (data.result || []).map(function(file) {
        			return new Item(file, self.currentPath);
        		});
        		self.buildTree(path);
        	});	
        };
        
        
        PolicyNavigator.prototype.buildTree = function(path) {
            var flatNodes = [], selectedNode = {};

            function recursive(parent, item, path) {
                var absName = path ? (path + '/' + item.model.name) : item.model.name;
                if (parent.name.trim() && path.trim().indexOf(parent.name) !== 0) {
                    parent.nodes = [];
                }
                if (parent.name !== path) {
                    for (var i in parent.nodes) {
                        recursive(parent.nodes[i], item, path);
                    }
                } else {
                    for (var e in parent.nodes) {
                        if (parent.nodes[e].name === absName) {
                            return;
                        }
                    }
                    parent.nodes.push({item: item, name: absName, nodes: []});
                }
                parent.nodes = parent.nodes.sort(function(a, b) {
                    return a.name.toLowerCase() < b.name.toLowerCase() ? -1 : a.name.toLowerCase() === b.name.toLowerCase() ? 0 : 1;
                });
            }

            function flatten(node, array) {
                array.push(node);
                for (var n in node.nodes) {
                    flatten(node.nodes[n], array);
                }
            }

            function findNode(data, path) {
                return data.filter(function (n) {
                    return n.name === path;
                })[0];
            }

            !this.history.length && this.history.push({name: '', nodes: []});
            flatten(this.history[0], flatNodes);
            selectedNode = findNode(flatNodes, path);
            selectedNode.nodes = [];

            for (var o in this.fileList) {
                var item = this.fileList[o];
                item.isFolder() && recursive(this.history[0], item, path);
            }
        };

        PolicyNavigator.prototype.folderClick = function(item) {
            this.currentPath = [];
            if (item && item.isFolder()) {
                this.currentPath = item.model.fullPath().split('/').splice(1);
            }
            this.refresh();
        };

        PolicyNavigator.prototype.upDir = function() {
            if (this.currentPath[0]) {
                this.currentPath = this.currentPath.slice(0, -1);
                this.refresh();
            }
        };

        PolicyNavigator.prototype.goTo = function(index) {
            this.currentPath = this.currentPath.slice(0, index + 1);
            this.refresh();
        };

        PolicyNavigator.prototype.fileNameExists = function(fileName) {
            for (var item in this.fileList) {
                item = this.fileList[item];
                if (fileName.trim && item.model.name.trim() === fileName.trim()) {
                    return true;
                }
            }
        };

        PolicyNavigator.prototype.listHasFolders = function() {
            for (var item in this.fileList) {
                if (this.fileList[item].model.type === 'dir') {
                    return true;
                }
            }
        };

        return PolicyNavigator;
    }]);
})(angular);